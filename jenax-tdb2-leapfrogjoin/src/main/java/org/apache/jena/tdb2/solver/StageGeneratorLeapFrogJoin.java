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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.GraphTDB;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderProc;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.GraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.store.tupletable.TupleTable;

/**
 * Stage generator for leap frog joins in TDB2.
 * <p>
 * This stage generator implements a "leap frog" join algorithm that efficiently
 * joins multiple basic graph patterns by leveraging the sorted nature of TDB2's
 * B+Tree indices. The algorithm coordinates iterators to skip non-matching ranges
 * rather than materializing all results.
 * <p>
 * The leap frog join works best when:
 * <ul>
 *   <li>There are multiple patterns with shared variables</li>
 *   <li>The indices provide sorted access to the data</li>
 *   <li>Memory is limited (avoids materialization)</li>
 * </ul>
 * <p>
 * This is an alternative to:
 * <ul>
 *   <li>Hash joins (which materialize one side)</li>
 *   <li>Nested loop joins (which do O(n*m) comparisons)</li>
 * </ul>
 */
public class StageGeneratorLeapFrogJoin implements StageGenerator {

    private final StageGenerator above;

    public StageGeneratorLeapFrogJoin(StageGenerator original) {
        this.above = original;
    }

    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        if (pattern.size() < 2) {
            return above.execute(pattern, input, execCxt);
        }

        Graph graph = execCxt.getActiveGraph();
        if ( !(graph instanceof GraphTDB) ) {
            return above.execute(pattern, input, execCxt);
        }

        GraphTDB graphTDB = (GraphTDB) graph;

        NodeTable nodeTable = graphTDB.getNodeTupleTable().getNodeTable();
        NodeTupleTable nodeTupleTable = graphTDB.getNodeTupleTable();

        // Use QueryIterRepeatApply to apply leap frog for each input binding
        return new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                // Substitute the binding into the pattern
                BasicPattern substituted = Substitute.substitute(pattern, binding);

                if (substituted.isEmpty()) {
                    return QueryIterRoot.create(execCxt);
                }

                // Partition into connected components
                List<List<Triple>> components = ComponentPartitioner.partition(substituted);

                // Handle edge cases
                if (components.size() == 0) {
                    return QueryIterRoot.create(execCxt);
                }

                if (components.size() == 1) {
                    // Single component - execute directly
                    return executeComponent(components.get(0), binding, graphTDB, nodeTable, nodeTupleTable, execCxt);
                }

                // Multiple components - use QueryIterCommonParent to combine results (Cartesian product)
                QueryIterator result = executeComponent(components.get(0), binding, graphTDB, nodeTable, nodeTupleTable, execCxt);
                for (int i = 1; i < components.size(); i++) {
                    final List<Triple> nextComponent = components.get(i);
                    final ExecutionContext fExecCxt = execCxt;
                    final GraphTDB fGraph = graphTDB;
                    final NodeTable fNodeTable = nodeTable;
                    final NodeTupleTable fNodeTupleTable = nodeTupleTable;

                    result = new QueryIterRepeatApply(result, execCxt) {
                        @Override
                        protected QueryIterator nextStage(Binding b) {
                            QueryIterator nextResults = executeComponent(nextComponent, binding, fGraph, fNodeTable, fNodeTupleTable, fExecCxt);
                            return new QueryIterCommonParent(nextResults, b, fExecCxt);
                        }
                    };
                }
                return result;
            }
        };
    }

    private QueryIterator executeComponent(List<Triple> component, Binding input,
                                            GraphTDB graph, NodeTable nodeTable,
                                            NodeTupleTable nodeTupleTable,
                                            ExecutionContext execCxt) {
        if (component.size() == 1) {
            // Single triple - use standard execution
            BasicPattern single = new BasicPattern();
            single.add(component.get(0));
            return above.execute(single, QueryIterRoot.create(execCxt), execCxt);
        }

        // Multiple triples - order by selectivity using ReorderFixed
        BasicPattern componentPattern = new BasicPattern(component);
        ReorderProc reorderProc = ReorderLib.fixed().reorderIndexes(componentPattern);
        List<Triple> orderedPatterns = reorderProc.reorder(componentPattern).getList();

        // Check if ordered patterns have shared variables
        List<Var> joinVars = findJoinVariables(orderedPatterns);

        if (joinVars.isEmpty()) {
            // No shared variables - use standard execution
            return above.execute(componentPattern, QueryIterRoot.create(execCxt), execCxt);
        }

        // Create the standard query iterators
        List<QueryIterator> patternIterators = new ArrayList<>();
        
        // Get the filter predicate from context
        Predicate<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext());

        // Determine graph node
        Node graphNode = null;
        if (nodeTupleTable.getTupleTable().getTupleLen() == 4) {
            graphNode = graph.getGraphName();
        }

        for (Triple triple : orderedPatterns) {
            BasicPattern singlePattern = new BasicPattern();
            singlePattern.add(triple);
            QueryIterator iter = above.execute(singlePattern, QueryIterRoot.create(execCxt), execCxt);
            patternIterators.add(iter);
        }

        // Create the leap frog join iterator with seek capability
        return new LeapFrogJoinIteratorOptimized(patternIterators, joinVars, execCxt, nodeTable, 
                                                  orderedPatterns, graphNode, filter, nodeTupleTable);
    }

    /**
     * Create a LeapFrogJoinIteratorOptimized for a given BGP.
     * <p>
     * This is a utility method for testing and direct iterator construction.
     * It creates individual iterators for each triple pattern and combines them
     * into a leap frog join iterator that tracks execution statistics.
     *
     * @param bgp the basic graph pattern to execute (must have at least 2 triples)
     * @param dsg the dataset graph containing the data
     * @param execCxt the execution context
     * @return a LeapFrogJoinIteratorOptimized with stats tracking enabled
     * @throws IllegalArgumentException if the BGP has fewer than 2 triples or no join variables
     */
    public static LeapFrogJoinIteratorOptimized createLeapFrogIterator(
            BasicPattern bgp, 
            DatasetGraphTDB dsg, 
            ExecutionContext execCxt) {
        
        List<Triple> triples = bgp.getList();
        if (triples.size() < 2) {
            throw new IllegalArgumentException("BGP must have at least 2 triples for leap frog join");
        }
        
        // Get join variables
        List<Var> joinVars = findJoinVariables(triples);
        if (joinVars.isEmpty()) {
            throw new IllegalArgumentException("No common join variables found in BGP");
        }
        
        // Get TDB2 infrastructure
        GraphTDB graphTDB = (GraphTDB) dsg.getDefaultGraph();
        NodeTable nodeTable = graphTDB.getNodeTupleTable().getNodeTable();
        NodeTupleTable nodeTupleTable = graphTDB.getNodeTupleTable();
        
        // Get standard stage generator for executing individual patterns
        StageGenerator standardSG = StageBuilder.chooseStageGenerator(execCxt.getContext());
        
        // Create iterators for each triple pattern
        List<QueryIterator> patternIterators = new ArrayList<>();
        for (Triple triple : triples) {
            BasicPattern single = new BasicPattern();
            single.add(triple);
            QueryIterator iter = standardSG.execute(single, QueryIterRoot.create(execCxt), execCxt);
            patternIterators.add(iter);
        }
        
        // Determine graph node (for quad support)
        Node graphNode = null;
        if (nodeTupleTable.getTupleTable().getTupleLen() == 4) {
            graphNode = graphTDB.getGraphName();
        }
        
        // Get filter predicate
        Predicate<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext());
        
        // Create and return the leap frog iterator
        return new LeapFrogJoinIteratorOptimized(
            patternIterators, 
            joinVars, 
            execCxt, 
            nodeTable, 
            triples, 
            graphNode, 
            filter, 
            nodeTupleTable
        );
    }

    /**
     * Find variables that appear in ALL triples of a component.
     * These are the join variables used by the leap frog join algorithm.
     *
     * @param component list of triples to analyze
     * @return list of variables common to all triples
     */
    public static List<Var> findJoinVariables(List<Triple> component) {
        if (component.size() < 2) {
            return new ArrayList<>();
        }

        Set<Var> commonVars = null;
        for (Triple triple : component) {
            Set<Var> vars = tripleVars(triple);
            if (commonVars == null) {
                commonVars = vars;
            } else {
                commonVars.retainAll(vars);
            }
        }

        if (commonVars == null || commonVars.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(commonVars);
    }

    /**
     * Get all variables mentioned in a triple.
     */
    public static Set<Var> tripleVars(Triple triple) {
        Set<Var> result = new java.util.HashSet<>();
        if (triple.getSubject().isVariable()) {
            result.add(Var.alloc(triple.getSubject()));
        }
        if (triple.getPredicate().isVariable()) {
            result.add(Var.alloc(triple.getPredicate()));
        }
        if (triple.getObject().isVariable()) {
            result.add(Var.alloc(triple.getObject()));
        }
        return result;
    }
}
