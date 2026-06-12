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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.MinMaxPriorityQueue;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.join.JoinKey;
import org.apache.jena.tdb2.lib.TupleLib;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.NodeIdType;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.store.tupletable.TupleTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
public class QueryIterLeapFrogJoin extends QueryIter {
    private static final Logger LOG = LoggerFactory.getLogger(QueryIterLeapFrogJoin.class);
    private static final boolean DEBUG = false;

    private final JoinKey joinKey;
    private final List<Triple> patternTriples;

    // private final List<QueryIterator> inputs;

    private final Node graphNode;
    private final NodeTupleTable nodeTupleTable;
    private final Predicate<Tuple<NodeId>> filter;
    private final TupleTable tupleTable;

    // private final Comparator<BindingNodeId> bindingComparator;

    private final Map<String, TupleIndexRecord> bestIndexCache;
    private final LeapFrogJoinStats stats = new LeapFrogJoinStats();
    private final MinMaxPriorityQueue<IteratorState> queue;

    private BindingNodeId slot;
    private boolean finished;
    private boolean finishedAfterCurrent;
    private boolean isInitialized = false;
    private final ExecutionContext execCxt;
    private final NodeTable nodeTable;

    public QueryIterLeapFrogJoin(
            //List<QueryIterator> inputs,
                                    JoinKey joinKey,
                                      ExecutionContext execCxt, NodeTable nodeTable,
                                      List<Triple> patternTriples, Node graphNode,
                                      Predicate<Tuple<NodeId>> filter,
                                      NodeTupleTable nodeTupleTable) {
        super(execCxt);

        if (joinKey.isEmpty()) {
            // TODO Probably we should close all involved iterators if this construction fails!
            //      Or we introduce a static helper that returns an QueryIterFailed.
            throw new IllegalArgumentException("Leap frog join requires at least one join variable");
        }

        this.joinKey = joinKey;
        // this.inputs = inputs;
        this.execCxt = execCxt;
        this.nodeTable = nodeTable;
        this.patternTriples = patternTriples;
        this.graphNode = graphNode;
        this.filter = filter;
        this.nodeTupleTable = nodeTupleTable;
        this.tupleTable = (nodeTupleTable != null) ? nodeTupleTable.getTupleTable() : null;
        this.finished = false;
        this.slot = null;
        this.isInitialized = false;

        this.bestIndexCache = new HashMap<>();

        // this.bindingComparator = BindingNodeIdComparator.of(joinKey);

        // Create comparator for ordering iterators by their current binding
        Comparator<IteratorState> byBinding = (s1, s2) -> {
            if (!s1.hasCurrent() && !s2.hasCurrent()) return 0;
            if (!s1.hasCurrent()) return -1;  // Exhausted iterators at end
            if (!s2.hasCurrent()) return 1;
            return compareBindings(s1.peekCurrentBinding(), s2.peekCurrentBinding());
        };

        // Create MinMaxPriorityQueue for O(1) min/max access and O(log n) operations
        this.queue = MinMaxPriorityQueue
            .orderedBy(byBinding)
            .maximumSize(patternTriples.size() + 1)
            .expectedSize(patternTriples.size())
            .create();
    }

   @Override
    protected boolean hasNextBinding() {
        if (slot != null) {
            return true;
        }
        if (finished) {
            return false;
        }
        if (finishedAfterCurrent) {
            finished = true;
            return false;
        }
        slot = moveToNextBindingOrNull();
        if (slot == null) {
            finished = true;
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
     * State of an iterator in the leap frog join.
     * When jumping, the current iterator is replaced with the jumped one.
     */
    private static class IteratorState {
        private NodeTable nodeTable;
        private final int patternIndex;

        private QueryIterPeek peekIter;
        private BindingNodeId currentBinding;
        private BindingNodeId nextBinding;

        IteratorState(NodeTable nodeTable, int patternIndex) {
            this.nodeTable = nodeTable;
            this.currentBinding = null;
            this.patternIndex = patternIndex;
        }

        int getPatternIndex() {
            return patternIndex;
        }

        QueryIterPeek getPeekIter() {
            return peekIter;
        }

        void setPeekIter(QueryIterPeek peekIter) {
            this.peekIter = peekIter;
            this.currentBinding = null;
            this.nextBinding = null;
        }

        boolean hasCurrent() {
            peekCurrentBinding();
            return currentBinding != null;
        }

        BindingNodeId peekCurrentBinding() {
            if (currentBinding == null) {
                if (peekIter.hasNext()) {
                    Binding b = peekIter.next();
                    currentBinding = BindingIdConverter.convert(b, nodeTable);
                }
            }
            return currentBinding;
        }

        BindingNodeId peekNextBinding() {
            peekCurrentBinding();
            if (nextBinding == null && peekIter.hasNext()) {
                Binding b = peekIter.next();
                nextBinding = BindingIdConverter.convert(b, nodeTable);
            }
            return nextBinding;
        }

        boolean hasNext() {
            return peekCurrentBinding() == null;
        }

        BindingNodeId next() {
            BindingNodeId r = peekCurrentBinding();
            currentBinding = nextBinding;
            nextBinding = null;
            return r;
        }
    }

    private BindingNodeId moveToNextBindingOrNull() {
        // Lazy initialization
        if (!isInitialized) {
            isInitialized = true;
            for (int i = 0; i < patternTriples.size(); i++) {
                IteratorState itState = new IteratorState(nodeTable, i);
                seekViaBPlusTree(itState, null);

                if (!itState.hasCurrent()) {
                    finished = true;
                    return null;
                }
                queue.add(itState);
            }
        }

        while (!finished) {
            stats.incrementIterations();

            // Peek at min/max - O(1)
            IteratorState minState = queue.peekFirst();
            IteratorState maxState = queue.peekLast();

            // Check for exhaustion
            if (!minState.hasCurrent()) {
                finished = true;
                return null;
            }

            BindingNodeId minBinding = minState.peekCurrentBinding();
            BindingNodeId maxBinding = maxState.peekCurrentBinding();

            // Check alignment: min == max means all iterators at same value for the join variables
            if (compareBindings(minBinding, maxBinding) == 0) {
                BindingNodeId savedSlot = null;

                // All aligned - attempt merge
                Binding mergedResult = tryMergeBindings();
                if (mergedResult != null) {
                    stats.incrementMergeSuccessCount();
                    savedSlot = BindingIdConverter.convert(mergedResult, nodeTable);
                } else {
                    stats.incrementMergeFailCount();
                }

                // Find the next least join key value among the iterators.
                BindingNodeId leastMatch = null;
                IteratorState match = null;
                for (IteratorState itx : queue) {
                    BindingNodeId cand = itx.peekNextBinding();
                    if (cand != null) { // Skip iterators that have no item after the current
                        if (leastMatch == null) {
                            leastMatch = cand;
                            match = itx;
                        } else {
                            if (compareBindings(cand, leastMatch) < 0) {
                                leastMatch = cand;
                                match = itx;
                            }
                        }
                    }
                }

                if (match == null) {
                    finished = true;
                } else {
                    // Remove iterator from queue, increment and re-insert
                    queue.remove(match);
                    match.next(); // We checked that peekNextBinding != null!
                    queue.add(match);
                }

                if (savedSlot != null) {
                    return savedSlot;
                }
            } else {
                // Not aligned - seek min toward max
                // Use pollFirst() to remove min - O(log n)
                IteratorState toSeek = queue.pollFirst();
                seekViaBPlusTree(toSeek, maxBinding);
                queue.offer(toSeek);  // Re-insert - O(log n)
            }
        }

        finished = true;
        return null;
    }

    /**
     * Seek using direct B+Tree range query through TupleIndexRecord.
     * This leverages the B+Tree's O(log n) seek capability.
     */
    private boolean seekViaBPlusTree(IteratorState state, BindingNodeId maxBinding) {

        // If the next binding is already the max one, then just call next instead of jumping.
        if (state.getPeekIter() != null) {
            BindingNodeId peek = state.peekNextBinding();
            if (peek != null && compareBindings(peek, maxBinding) == 0) {
                state.next();
                return true;
            }
        }

        int patternIndex = state.getPatternIndex();
        Triple triple = patternTriples.get(patternIndex);

        try {
            // Build a pattern tuple for the B+Tree seek
            Tuple<NodeId> seekPattern = buildSeekPatternForTriple(triple, null);
            if (seekPattern == null) {
                if (DEBUG) {
                    LOG.debug("Failed to build seek pattern for pattern {}", patternIndex);
                }
                return false;
            }

            // Find the best index for this seek pattern
            TupleIndexRecord bestIndex = findBestIndexForPattern(patternIndex, triple, seekPattern);
            if (bestIndex == null) {
                if (DEBUG) {
                    LOG.debug("No suitable index found for pattern {}", patternIndex);
                }
                return false;
            }

            Tuple<NodeId> findPattern = buildSeekPatternForTriple(triple, maxBinding);


            // TODO Must also consider maxBinding - joinKey-slots must be set to values of maxBinding
            Tuple<NodeId> minPattern = anyToMin(findPattern);
            Tuple<NodeId> maxPattern = anyToMax(findPattern);

            // TODO From the max binding, extract the joining values and use them in the right way
            // to obtain an iterator over the range.

            RangeIndex rangeIndex = bestIndex.getRangeIndex();
            TupleMap tm = bestIndex.getMapping();
            RecordFactory rf = rangeIndex.getRecordFactory();
            Record min = rangeIndex.minKey();
            Record max = rangeIndex.maxKey();
            Record minRecord = TupleLib.record(rf, minPattern, tm);
            Record maxRecord = TupleLib.record(rf, maxPattern, tm);
            // TODO Next line: Can we use null? Or NodeId.NodeIdAny? Or perhaps create a node id with a max value?
            Iterator<Record> it = rangeIndex.iterator(minRecord, maxRecord);
            Iterator<Tuple<NodeId>> tupleIter = Iter.map(it, rec -> TupleLib.tuple(rec, tm));

            if (DEBUG) {
                LOG.debug("Using index {} for pattern {}", bestIndex.getClass().getSimpleName(), patternIndex);
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
                Binding b = builder.build();
                System.err.println("Binding for " + triple + ": " + b);
                return b;
            });

            // Wrap in new QueryIterPeek, closing the old one
            QueryIterPeek oldPeekIter = state.getPeekIter();
            if (oldPeekIter != null) {
                oldPeekIter.close();
            }
            QueryIterPeek newPeekIter = QueryIterPeek.create(
                    QueryIterPlainWrapper.create(bindingIter, execCxt), execCxt);
            state.setPeekIter(newPeekIter);

            if (false) {
                System.out.println("Data for " + triple);
                while(state.getPeekIter().hasNext()) {
                    System.out.println("  Next: " + state.getPeekIter().next());
                }
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
     * Returns null if a constant of the triple could not be mapped to a NodeId.
     */
    private Tuple<NodeId> buildSeekPatternForTriple(Triple triple, BindingNodeId binding) {
        int n = 3;
        NodeId[] ids = new NodeId[n];
        int i;
        for (i = 0; i < n; i++) {
            Node node = getNode(triple, i);
            if (Var.isVar(node)) {
                Var v = Var.alloc(node);
                NodeId nid;
                if (binding != null) {
                    nid = binding.get(v);
                    if (nid == null) {
                        nid = NodeId.NodeIdAny;
                    }
                } else {
                    nid = NodeId.NodeIdAny;
                }
                ids[i] = nid;
            } else {
                NodeId nid = nodeTable.getNodeIdForNode(node);
                if (nid == null) {
                    break;
                }
                ids[i] = nid;
            }
        }
        return i < n ? null : TupleFactory.create(ids);
    }

    private static Tuple<NodeId> anyToMin(Tuple<NodeId> tuple) {
        int n = tuple.len();
        NodeId[] arr = new NodeId[n];
        for (int i = 0; i < n; ++i) {
            NodeId x = tuple.get(i);
            arr[i] = NodeId.isAny(x) ? NodeIdFactory.createPtr(0) : x;
        }
        return TupleFactory.create(arr);
    }

    private static Tuple<NodeId> anyToMax(Tuple<NodeId> tuple) {
        int n = tuple.len();
        NodeId[] arr = new NodeId[n];
        for (int i = 0; i < n; ++i) {
            NodeId x = tuple.get(i);
            arr[i] = NodeId.isAny(x) ? NodeId.createRaw(NodeIdType.SPECIAL, Long.MAX_VALUE) : x;
        }
        return TupleFactory.create(arr);
    }

    /**
     * Find the best TupleIndexRecord for a given pattern.
     * Uses the same weight-based index selection as TupleTable.find().
     * <p>
     * Cache key is based on patternIndex AND the pattern structure (which vars are bound).
     * This ensures we get the correct best index for different seek patterns.
     */
    private TupleIndexRecord findBestIndexForPattern(int patternIndex, Triple triple, Tuple<NodeId> pattern) {
        // TODO Use either triple or tuple but not both.
        String cacheKey = patternIndex + ":" + buildPatternKey(pattern);
        if (bestIndexCache.containsKey(cacheKey)) {
            stats.incrementIndexCacheHits();
            return bestIndexCache.get(cacheKey);
        }
        stats.incrementIndexCacheMisses();

        int constSlots = 0;
        for (int i = 0; i < 3; ++i) {
            if (getNode(triple, i).isConcrete()) {
                ++constSlots;
            }
        }

        int bestWeight = -1;
        TupleIndexRecord bestIndex = null;
        // So we want an index where all constants of the triple come first, followed by the joinkey in the right order.
        // ?s :p ?s -> PO* PS*

        // FIXME: We would probably need additional like PSO - for each predicate, give the subjects in order.
        //        We need to make sure that we select the right index for the join variable.
        for (TupleIndex idx : tupleTable.getIndexes()) {
            TupleMap tm = idx.getMapping();
            int i;
            int n = idx.getTupleLength();
            for (i = 0; i < n; ++i) {
                int tupleSlot = tm.mapIdx(i);
                Node node = getNode(triple, tupleSlot);
                // NodeId node = pattern.get(tupleSlot);
                if (!node.isConcrete()) {
                    break;
                }
            }
            int constPrefixLen = i;

            // Note: we could post-filter by constants if constPrefixLen < constSlots
            if (constPrefixLen != constSlots) {
                continue;
            }

            // The next slots of the index must in order map to the join key variables
            int j;
            int jn = joinKey.size();
            for (j = 0; i < n && j < jn; ++i, ++j) {
                Node expected = joinKey.get(j);
                int tupleSlot = tm.mapIdx(i);
                Node actual = getNode(triple, tupleSlot);
                if (!actual.equals(expected)) {
                    break;
                }
            }

            if (j != jn) {
                // Index is unsuitable because its slots did not match the join key.
                continue;
            }

            // If remaining index slots map to constants, we need to post-filter by those.
//            for (; i < n; ++i) {
//                int tupleSlot = tm.mapIdx(i);
//                Node node = getNode(triple, tupleSlot);
//                if (nod)
//            }

            // System.err.println(idx + ": " + triple + " i - j " + constPrefixLen + " - " + j);


            // tm.mapIdx(0); // map index slots to SPO. if index is POS, then mapIdx(0) -> 1
            //tm.putSlotIdx( ); // SPO to the pos in the index. if index is POS, then putSlotIdx(0) -> 2

            // So: iterate from 0 to 3
            //     use tm.mapIndex(i) to check if the index slot maps to a constant in the triple
            //     remaining tuple slots must map to variables in the triple that in order match the join key.

            if (idx instanceof TupleIndexRecord record) { // only TupleIndexRecord has getRangeIndex()
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

    /** Access a triple's component by a zero-based index in order s, p, o.
     * Raises {@link IndexOutOfBoundsException} for any index outside of the range [0, 2]*/
    public static Node getNode(Triple triple, int idx) {
        switch (idx) {
        case 0: return triple.getSubject();
        case 1: return triple.getPredicate();
        case 2: return triple.getObject();
        default: throw new IndexOutOfBoundsException("Cannot access index " + idx + " of a triple");
        }
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
        Tuple<Node> result = (graphNode == null)
            ? TupleFactory.create3(triple.getSubject(), triple.getPredicate(), triple.getObject())
            : TupleFactory.create4(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject());
        return result;
    }

    /** Util to peek the BindingNodeId from a QueryIterPeek */
    private BindingNodeId peek(QueryIterPeek it) {
        return peek(it, nodeTable);
    }

    private static BindingNodeId peek(QueryIterPeek it, NodeTable nodeTable) {
        Binding raw = it.peek();
        BindingNodeId result = raw == null ? null : BindingIdConverter.convert(raw, nodeTable);
        return result;
    }

    /**
     * Try to merge all current bindings into a single result.
     * Returns null if merge fails (binding conflict).
     */
    private Binding tryMergeBindings() {
        Binding result = null;
        for (IteratorState state : queue) {
            BindingNodeId current = state.peekCurrentBinding();
            if (current == null) {
                return null;
            }
            Binding converted = BindingIdConverter.convToBinding(current, nodeTable);
            result = result == null ? converted : Algebra.merge(result, converted);
            if (result == null) {
                break;
            }
        }
        return result;
    }

    /**
     * Compare two bindings on join variables.
     * Both bindings are guaranteed non-null (enforced by heap invariants).
     * All joinVars are guaranteed present in every binding (enforced by pattern execution).
     */
    private int compareBindings(BindingNodeId left, BindingNodeId right) {
        int result = BindingNodeIdComparator.compare(joinKey, left, right);
        // stats.addComparisons(comparisons);
        return result;
    }

    @Override
    protected void closeIterator() {
        for (IteratorState state : queue) {
            if (state.peekIter != null) {
                state.peekIter.close();
            }
        }
        queue.clear();
    }

    @Override
    protected void requestCancel() {
        for (IteratorState state : queue) {
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

