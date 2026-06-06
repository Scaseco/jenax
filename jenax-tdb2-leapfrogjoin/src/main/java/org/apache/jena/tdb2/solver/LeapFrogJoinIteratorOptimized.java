/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.tdb2.store.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.store.tupletable.TupleTable;

/**
 * N-way leap frog join for TDB2 that leverages B+Tree sorted indices.
 * <p>
 * This join algorithm coordinates multiple sorted iterators to efficiently
 * find matching bindings by using peek operations to find the minimum binding
 * and seeking (jumping) iterators to the appropriate position instead of
 * stepping through them one at a time.
 * <p>
 * Algorithm:
 * <ol>
 *   <li>Initialize all iterators to their first binding</li>
 *   <li>Find the minimum binding across all iterators (min-heap)</li>
 *   <li>If all iterators are at or past the minimum:
 *     <ul>
 *       <li>Try to merge bindings into a join result</li>
 *       <li>If merge succeeds: advance the minimum iterator (one step)</li>
 *     </ul>
 *   </li>
 *   <li>If not all at minimum:
 *     <ul>
 *       <li>Advance all iterators currently at the minimum value (one step)</li>
 *       <li>Seek (jump) iterators behind the minimum to the minimum value using B+Tree range query</li>
 *     </ul>
 *   </li>
 *   <li>Repeat from step 2 until exhausted</li>
 * </ol>
 * <p>
 * Key optimizations:
 * <ul>
 *   <li>Uses min-heap for O(1) minimum finding (instead of O(m) scan)</li>
 *   <li>Uses QueryIterPeek to peek at bindings without consuming</li>
 *   <li>Only advances iterators when necessary</li>
 *   <li>Performs leap frog coordination using NodeId comparisons</li>
 *   <li>Seeks (jumps) iterators to the minimum value using B+Tree range queries - O(log n) vs O(n) sequential</li>
 * </ul>
 */
public class LeapFrogJoinIteratorOptimized extends QueryIter {
    private static final Logger LOG = LoggerFactory.getLogger(LeapFrogJoinIteratorOptimized.class);
    private static final boolean DEBUG = false;

    private final List<Var> joinVars;
    private final List<IteratorState> iteratorStates;
    private final List<Triple> patternTriples;
    private final Node graphNode;
    private final NodeTupleTable nodeTupleTable;
    private final Predicate<Tuple<NodeId>> filter;
    private final TupleTable tupleTable;

    private final Map<String, TupleIndexRecord> bestIndexCache;
    private final LeapFrogJoinStats stats = new LeapFrogJoinStats();

    private BindingNodeId slot;
    private boolean finished;
    private boolean finishedAfterCurrent;
    private boolean isInitialized = false;
    private final ExecutionContext execCxt;
    private final NodeTable nodeTable;

    public LeapFrogJoinIteratorOptimized(List<QueryIterator> inputs, List<Var> joinVars,
                                          ExecutionContext execCxt, NodeTable nodeTable,
                                          List<Triple> patternTriples, Node graphNode,
                                          Predicate<Tuple<NodeId>> filter,
                                          NodeTupleTable nodeTupleTable) {
        super(execCxt);
        this.joinVars = joinVars;
        this.execCxt = execCxt;
        this.nodeTable = nodeTable;
        this.patternTriples = patternTriples;
        this.graphNode = graphNode;
        this.filter = filter;
        this.nodeTupleTable = nodeTupleTable;
        this.tupleTable = (nodeTupleTable != null) ? nodeTupleTable.getTupleTable() : null;
        this.finished = false;
        this.slot = null;

        iteratorStates = new ArrayList<>();
        this.bestIndexCache = new HashMap<>();

        if (joinVars.isEmpty()) {
            throw new IllegalArgumentException("Leap frog join requires at least one join variable");
        }

        // Build IteratorState with standard iterator
        for (int i = 0; i < inputs.size(); i++) {
            QueryIterator input = inputs.get(i);
            QueryIterPeek peekIter = (input != null) ? QueryIterPeek.create(input, execCxt) : null;
            iteratorStates.add(new IteratorState(peekIter));
        }
    }

   @Override
    protected boolean hasNextBinding() {
        if (finished) {
            return false;
        }
        if (slot != null) {
            return true;
        }
        if (finishedAfterCurrent) {
            finished = true;
            return false;
        }
        slot = moveToNextBindingOrNull();
        if (slot == null) {
            close();
            return false;
        }
        return true;
    }

     @Override
    protected Binding moveToNextBinding() {
        Binding result = BindingIdConverter.convToBinding(slot, nodeTable);
        slot = null;
        if (finishedAfterCurrent) {
            finished = true;
        }
        return result;
    }

    /**
     * State of an iterator in the leap frog join, with heap index for priority queue operations.
     */
    private static class IteratorState {
        private QueryIterPeek peekIter;
        private BindingNodeId currentBinding;
        private int heapIndex;

        IteratorState(QueryIterPeek peekIter) {
            this.peekIter = peekIter;
            this.currentBinding = null;
            this.heapIndex = -1;
        }

        QueryIterPeek getPeekIter() {
            return peekIter;
        }

        void setPeekIter(QueryIterPeek peekIter) {
            this.peekIter = peekIter;
        }

        BindingNodeId getCurrentBinding() {
            return currentBinding;
        }

        void setCurrentBinding(BindingNodeId binding) {
            this.currentBinding = binding;
        }

        int getHeapIndex() {
            return heapIndex;
        }

        void setHeapIndex(int index) {
            this.heapIndex = index;
        }

        boolean hasCurrent() {
            return currentBinding != null;
        }
    }

    private BindingNodeId moveToNextBindingOrNull() {
        // Lazy initialization: only once per iterator lifecycle
        if (!isInitialized) {
            if (!initializeState()) {
                finished = true;
                return null;
            }
            stats.incrementHeapRebuildCount();
            buildHeap();
            isInitialized = true;
        }

        while (!finished) {
            stats.incrementIterations();

            // Get minimum from heap (O(1) - heap[0])
            IteratorState minState = iteratorStates.get(0);
            if (minState == null || !minState.hasCurrent()) {
                finished = true;
                return null;
            }

            BindingNodeId minBinding = minState.getCurrentBinding();
            if (minBinding == null) {
                // Current min has no binding - advance it and rebuild
                if (!advanceIterator(minState)) {
                    finished = true;
                    return null;
                }
                if (!updateStateForIndex(0)) {
                    finished = true;
                    return null;
                }
                stats.incrementHeapifyCount();
                heapifyDown(0);
                continue;
            }

            // Check if all iterators are at the EXACT same binding (min == max)
            boolean allAligned = allAtSameBinding();

            if (allAligned) {
                  // All iterators match or are past the minimum - attempt to merge
                        Binding mergedResult = tryMergeBindings();
                  if (mergedResult != null) {
                      stats.incrementMergeSuccessCount();
                      // We have a join result - save it and advance ALL iterators at the minimum
                      BindingNodeId savedSlot = BindingIdConverter.convert(mergedResult, nodeTable);
                      int advancedCount = 0;
                      for (IteratorState state : iteratorStates) {
                          if (state.hasCurrent() && compareBindings(state.getCurrentBinding(), minBinding) == 0) {
                              if (!advanceIterator(state)) {
                                  finishedAfterCurrent = true;
                                  return savedSlot;
                              }
                              if (!updateStateForCurrent(state)) {
                                  finishedAfterCurrent = true;
                                  return savedSlot;
                              }
                              advancedCount++;
                          }
                      }
                      // Only one iterator changed (the common case) — O(log m)
                      if (advancedCount == 1) {
                          stats.incrementHeapifyCount();
                          heapifyDown(0);
                      } else {
                          // Multiple iterators changed — O(m) rebuild
                          stats.incrementHeapRebuildCount();
                          buildHeap();
                      }
                      // Don't set finished=true here — savedSlot must be consumed first
                      return savedSlot;
                  } else {
                      stats.incrementMergeFailCount();
                      // Merge failed (binding conflict) - use leap frog advance:
                      // advance iterators at the minimum, seek iterators behind it,
                      // leave iterators past the minimum where they are
                      if (!leapFrogAdvance(minBinding)) {
                          finished = true;
                          return null;
                      }
                      // leapFrogAdvance may modify multiple iterators — O(m) rebuild needed
                      stats.incrementHeapRebuildCount();
                      buildHeap();
                  }
              } else {
                  // Not all iterators aligned - leap frog:
                  // 1. Advance all iterators currently at the minimum value
                  // 2. Seek all iterators behind the minimum to the minimum value using B+Tree range query
                  if (!leapFrogAdvance(minBinding)) {
                      finished = true;
                      return null;
                  }
                 // leapFrogAdvance may modify multiple iterators — O(m) rebuild needed
                 stats.incrementHeapRebuildCount();
                 buildHeap();
            }
        }

        finished = true;
        return null;
    }

    /**
     * Perform a leap frog advance: advance minimum iterators and seek ahead others.
     * This is the core "leap frog" optimization - instead of stepping iterators
     * one by one, we seek them directly to the target position using B+Tree range queries.
     */
    private boolean leapFrogAdvance(BindingNodeId minBinding) {
        // First, advance all iterators that are AT the minimum value (one step each)
        for (IteratorState state : iteratorStates) {
            if (state.hasCurrent() && compareBindings(state.getCurrentBinding(), minBinding) == 0) {
                if (!advanceIterator(state)) {
                    return false;
                }
                if (!updateStateForCurrent(state)) {
                    return false;
                }
            }
        }

        // Then, seek ahead all iterators that are BEHIND the minimum.
        // Instead of calling .next() one by one (which could be O(n)),
        // we create a new B+Tree range iterator starting at minBinding - O(log n).
        for (int i = 0; i < iteratorStates.size(); i++) {
            IteratorState state = iteratorStates.get(i);
            if (state.hasCurrent() && compareBindings(state.getCurrentBinding(), minBinding) < 0) {
                if (!seekAhead(state, minBinding, i)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Advance a single iterator by one step (sequential, not seeking).
     */
    private boolean advanceIterator(IteratorState state) {
        QueryIterPeek peekIter = state.getPeekIter();
        if (peekIter == null || !peekIter.hasNext()) {
            return false;
        }
        peekIter.next();
        return updateStateForCurrent(state);
    }

    /**
     * Seek an iterator ahead to the minimum binding value using B+Tree range query.
     * <p>
     * This is the key optimization: instead of stepping through bindings one at a time,
     * we create a fresh B+Tree range iterator that starts directly at the minimum key.
     * The B+Tree's internalSearch() method navigates from root to the correct leaf in O(log n).
     * <p>
     * The seek works by:
     * 1. Building a pattern tuple with concrete NodeIds for join variables
     * 2. Calling TupleIndexRecord.find() which computes minRec/maxRec bounds
     * 3. BPlusTree.iterator(minRec, maxRec) which does O(log n) seek to minRec
     * 4. Wrapping the result in a new QueryIterPeek
     *
     * @param state the iterator state to seek
     * @param minBinding the binding to seek to
     * @param patternIndex the index of the pattern in the ordered patterns list
     * @return true if seek succeeded and there are more results
     */
    private boolean seekAhead(IteratorState state, BindingNodeId minBinding, int patternIndex) {
        Triple triple = patternTriples.get(patternIndex);

        // Always try B+Tree seek first - it's O(log n) vs O(n) for sequential
        boolean seeked = seekViaBPlusTree(state, minBinding, triple, patternIndex);
        if (seeked) {
            stats.incrementSeekCount();
            return true;
        }

        // Fallback: use sequential advance until we reach or pass the minimum
        stats.incrementStepCount();
        return seekViaSequentialAdvance(state, minBinding);
    }

    /**
     * Seek using direct B+Tree range query through TupleIndexRecord.
     * This leverages the B+Tree's O(log n) seek capability.
     */
    private boolean seekViaBPlusTree(IteratorState state, BindingNodeId minBinding, Triple triple, int patternIndex) {
        try {
            if (DEBUG) {
                LOG.debug("Attempting B+Tree seek for pattern {} with binding {}", patternIndex, minBinding);
            }

            // Build a pattern tuple for the B+Tree seek
            Tuple<NodeId> seekPattern = buildSeekPatternForTriple(minBinding, triple);
            if (seekPattern == null) {
                if (DEBUG) {
                    LOG.debug("Failed to build seek pattern for pattern {}", patternIndex);
                }
                return false;
            }

            // Find the best index for this seek pattern
            TupleIndexRecord bestIndex = findBestIndexForPattern(patternIndex, seekPattern);
            if (bestIndex == null) {
                if (DEBUG) {
                    LOG.debug("No suitable index found for pattern {}", patternIndex);
                }
                return false;
            }

            if (DEBUG) {
                LOG.debug("Using index {} for pattern {}", bestIndex.getClass().getSimpleName(), patternIndex);
            }

            // Create a new iterator via the index's find method
            // This goes through TupleIndexRecord.findOrScan() -> BPlusTree.iterator(minRec, maxRec)
            // The BPlusTree will seek to minRec via internalSearch() - O(log n)
            Iterator<Tuple<NodeId>> tupleIter = bestIndex.find(seekPattern);

            // Apply filter if present
            if (filter != null) {
                tupleIter = Iter.filter(tupleIter, filter);
            }

            // Convert tuples to bindings
            Tuple<Node> nodePattern = tripleToNodeTuple(triple);

            Iterator<Binding> bindingIter = Iter.map(tupleIter, t -> {
                BindingBuilder builder = Binding.builder();
                for (int i = 0; i < t.len(); i++) {
                    NodeId nid = t.get(i);
                    if (NodeId.isConcrete(nid)) {
                        Node node = nodeTable.getNodeForNodeId(nid);
                        Node pnode = nodePattern.get(i);
                        if (pnode != null && Var.isVar(pnode)) {
                            builder.add(Var.alloc(pnode), node);
                        }
                    }
                }
                return builder.build();
            });

            // Wrap in new QueryIterPeek, closing the old one
            QueryIterPeek oldPeekIter = state.getPeekIter();
            if (oldPeekIter != null) {
                oldPeekIter.close();
            }
            QueryIterPeek newPeekIter = QueryIterPeek.create(
                    QueryIterPlainWrapper.create(bindingIter, execCxt), execCxt);
            state.setPeekIter(newPeekIter);

            // Get the first binding
            if (!newPeekIter.hasNext()) {
                if (DEBUG) {
                    LOG.debug("B+Tree seek returned no results for pattern {}", patternIndex);
                }
                return false;
            }
            BindingNodeId b = peek(newPeekIter);
            if (b == null) {
                if (DEBUG) {
                    LOG.debug("B+Tree seek returned null binding for pattern {}", patternIndex);
                }
                return false;
            }
            state.setCurrentBinding(b);
            if (DEBUG) {
                LOG.debug("B+Tree seek succeeded for pattern {}, new binding: {}", patternIndex, b);
            }
            return true;

        } catch (Exception e) {
            if (DEBUG) {
                LOG.debug("B+Tree seek failed for pattern {}: {}", patternIndex, e.getMessage());
            }
            return false;
        }
    }

    /**
     * Build a seek pattern tuple from a binding and triple.
     * Concrete nodes in the triple stay concrete, join variables get their target values.
     */
    private Tuple<NodeId> buildSeekPatternForTriple(BindingNodeId minBinding, Triple triple) {
        Tuple<Node> nodeTuple = tripleToNodeTuple(triple);
        int len = nodeTuple.len();
        NodeId[] ids = new NodeId[len];

        for (int i = 0; i < len; i++) {
            Node node = nodeTuple.get(i);
            if (Var.isVar(node)) {
                Var v = Var.alloc(node);
                NodeId nid = minBinding.get(v);
                if (nid != null && !NodeId.isAny(nid) && !NodeId.isDoesNotExist(nid)) {
                    ids[i] = nid;
                } else {
                    ids[i] = NodeId.NodeIdAny;
                }
            } else {
                NodeId nid = nodeTable.getNodeIdForNode(node);
                ids[i] = (nid != null) ? nid : NodeId.NodeIdAny;
            }
        }

        return TupleFactory.create(ids);
    }

    /**
     * Find the best TupleIndexRecord for a given pattern.
     * Uses the same weight-based index selection as TupleTable.find().
     * <p>
     * Cache key is based on patternIndex AND the pattern structure (which vars are bound).
     * This ensures we get the correct best index for different seek patterns.
     */
    private TupleIndexRecord findBestIndexForPattern(int patternIndex, Tuple<NodeId> pattern) {
        String cacheKey = patternIndex + ":" + buildPatternKey(pattern);
        if (bestIndexCache.containsKey(cacheKey)) {
            stats.incrementIndexCacheHits();
            return bestIndexCache.get(cacheKey);
        }
        stats.incrementIndexCacheMisses();

        int bestWeight = -1;
        TupleIndexRecord bestIndex = null;

        for (TupleIndex idx : tupleTable.getIndexes()) {
            if (idx instanceof TupleIndexRecord record) {
                int weight = record.weight(pattern);
                if (weight > bestWeight) {
                    bestWeight = weight;
                    bestIndex = record;
                }
            }
        }

        bestIndexCache.put(cacheKey, bestIndex);
        return bestIndex;
    }

    /**
     * Build a cache key based on which positions in the pattern are concrete vs wildcards.
     * This allows caching indices for the same pattern structure with different values.
     */
    private String buildPatternKey(Tuple<NodeId> pattern) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < pattern.len(); i++) {
            NodeId nid = pattern.get(i);
            if (NodeId.isConcrete(nid)) {
                key.append("C");
            } else {
                key.append("?");
            }
        }
        return key.toString();
    }

    /**
     * Convert a Triple to a node tuple (3-tuple for triples, 4-tuple for quads).
     */
    private Tuple<Node> tripleToNodeTuple(Triple triple) {
        if (graphNode == null) {
            return TupleFactory.create3(triple.getSubject(), triple.getPredicate(), triple.getObject());
        } else {
            return TupleFactory.create4(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject());
        }
    }

    /**
     * Sequential fallback: advance iterator step by step until we reach or pass the minimum.
     * This is O(n) but works when B+Tree seek is not available.
     */
    private boolean seekViaSequentialAdvance(IteratorState state, BindingNodeId minBinding) {
        QueryIterPeek peekIter = state.getPeekIter();
        if (peekIter == null) {
            return false;
        }
        while (peekIter.hasNext()) {
            BindingNodeId nextBinding = peek(peekIter);
            if (nextBinding == null) {
                return false;
            }
            if (compareBindings(nextBinding, minBinding) >= 0) {
                // Reached or passed the minimum - stop here
                peekIter.next();
                state.setCurrentBinding(nextBinding);
                return true;
            }
            peekIter.next();
        }
        return false;
    }

    private BindingNodeId peek(QueryIterPeek it) {
        if (it == null) {
            return null;
        }
        Binding raw = it.peek();
        if (raw == null) {
            return null;
        }
        BindingNodeId result = BindingIdConverter.convert(raw, nodeTable);
        return result;
    }

    /**
     * Update the binding state for a given iterator state.
     */
    private boolean updateStateForCurrent(IteratorState state) {
        QueryIterPeek peekIter = state.getPeekIter();
        if (peekIter == null) {
            return false;
        }
        if (!peekIter.hasNext()) {
            return false;
        }
        BindingNodeId b = peek(peekIter);
        state.setCurrentBinding(b);
        return true;
    }

    /**
     * Update the binding for a specific index in the iterator states list.
     */
    private boolean updateStateForIndex(int index) {
        IteratorState state = iteratorStates.get(index);
        return updateStateForCurrent(state);
    }

    /**
     * Initialize iterator state with first bindings from all iterators.
     */
    private boolean initializeState() {
        for (IteratorState state : iteratorStates) {
            QueryIterPeek peekIter = state.getPeekIter();
            if (peekIter == null || !peekIter.hasNext()) {
                return false;
            }
            BindingNodeId b = peek(peekIter);
            if (b == null) {
                return false;
            }
            state.setCurrentBinding(b);
            state.setHeapIndex(iteratorStates.indexOf(state));
        }
        return true;
    }

    /**
     * Build a min-heap from the current iterator states.
     */
    private void buildHeap() {
        int n = iteratorStates.size();
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    /**
     * Heapify down from given index to maintain min-heap property.
     */
    private void heapifyDown(int i) {
        int n = iteratorStates.size();
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && iteratorStates.get(left).hasCurrent() && iteratorStates.get(smallest).hasCurrent()
                && compareBindings(iteratorStates.get(left).getCurrentBinding(),
                        iteratorStates.get(smallest).getCurrentBinding()) < 0) {
            smallest = left;
        }

        if (right < n && iteratorStates.get(right).hasCurrent() && iteratorStates.get(smallest).hasCurrent()
                && compareBindings(iteratorStates.get(right).getCurrentBinding(),
                        iteratorStates.get(smallest).getCurrentBinding()) < 0) {
            smallest = right;
        }

        if (smallest != i) {
            swap(i, smallest);
            heapifyDown(smallest);
        }
    }

    /**
     * Swap two elements in the heap and update their heap indices.
     */
    private void swap(int i, int j) {
        IteratorState temp = iteratorStates.get(i);
        iteratorStates.set(i, iteratorStates.get(j));
        iteratorStates.set(j, temp);

        iteratorStates.get(i).setHeapIndex(i);
        iteratorStates.get(j).setHeapIndex(j);
    }

    /**
     * Check if all iterators are at or past the minimum binding.
     */
    private boolean allAtOrPastMinimum(BindingNodeId minBinding) {
        for (IteratorState state : iteratorStates) {
            if (!state.hasCurrent()) {
                return false;
            }
            if (compareBindings(state.getCurrentBinding(), minBinding) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if all iterators are at the EXACT same binding value (min == max).
     * This is the correct alignment check for leap frog join - all iterators must
     * be at the same value to attempt a merge.
     */
    private boolean allAtSameBinding() {
        if (iteratorStates.isEmpty()) {
            return false;
        }
        IteratorState firstState = iteratorStates.get(0);
        if (!firstState.hasCurrent()) {
            return false;
        }
        BindingNodeId firstBinding = firstState.getCurrentBinding();
        
        for (IteratorState state : iteratorStates) {
            if (!state.hasCurrent()) {
                return false;
            }
            if (compareBindings(state.getCurrentBinding(), firstBinding) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Try to merge all current bindings into a single result.
     * Returns null if merge fails (binding conflict).
     */
    private Binding tryMergeBindings() {
        if (iteratorStates.isEmpty()) {
            return null;
        }

        IteratorState firstState = iteratorStates.get(0);
        BindingNodeId firstBinding = firstState.getCurrentBinding();
        if (firstBinding == null) {
            return null;
        }

        Binding result = BindingIdConverter.convToBinding(firstBinding, nodeTable);

        for (int i = 1; i < iteratorStates.size(); i++) {
            IteratorState state = iteratorStates.get(i);
            BindingNodeId current = state.getCurrentBinding();
            if (current == null) {
                return null;
            }
            Binding converted = BindingIdConverter.convToBinding(current, nodeTable);
            result = mergeBindings(result, converted);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    /**
     * Merge two bindings, checking for conflicts.
     */
    private Binding mergeBindings(Binding left, Binding right) {
        Set<Var> leftVars = left.varsMentioned();

        for (Var var : leftVars) {
            Node leftNode = left.get(var);
            Node rightNode = right.get(var);
            if (rightNode != null && !leftNode.equals(rightNode)) {
                return null;
            }
        }

        BindingBuilder builder = Binding.builder(left);
        right.forEach((var, node) -> {
            if (!leftVars.contains(var)) {
                builder.add(var, node);
            }
        });
        return builder.build();
    }

    /**
     * Compare two bindings on join variables.
     * Both bindings are guaranteed non-null (enforced by heap invariants).
     * All joinVars are guaranteed present in every binding (enforced by pattern execution).
     */
    private int compareBindings(BindingNodeId left, BindingNodeId right) {
        int comparisons = 0;
        for (Var var : joinVars) {
            NodeId leftId = left.get(var);
            NodeId rightId = right.get(var);

            int cmp = leftId.compareTo(rightId);
            comparisons++;
            if (cmp != 0) {
                stats.addComparisons(comparisons);
                return cmp;
            }
        }
        stats.addComparisons(comparisons);

        return 0;
    }

    @Override
    protected void closeIterator() {
        for (IteratorState state : iteratorStates) {
            if (state.peekIter != null) {
                state.peekIter.close();
            }
        }
        iteratorStates.clear();
    }

    @Override
    protected void requestCancel() {
        for (IteratorState state : iteratorStates) {
            if (state.peekIter != null) {
                state.peekIter.cancel();
            }
        }
    }

    /**
     * Get the statistics for this leap frog join execution.
     * <p>
     * The stats object is updated during query execution and contains
     * the final metrics once the iterator is closed.
     *
     * @return the statistics object for this join execution
     */
    public LeapFrogJoinStats getStats() {
        return stats;
    }
}
