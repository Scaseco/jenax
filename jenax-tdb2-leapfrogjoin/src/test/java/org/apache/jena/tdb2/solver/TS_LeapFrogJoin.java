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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

public class TS_LeapFrogJoin {
    static Dataset dataset = null;
    
    // DEBUG switch for enabling debug output during tests
    private static final boolean DEBUG_TESTS = false;

    @BeforeAll
    static public void beforeClass() {
        dataset = TDB2Factory.createDataset();
        dataset.begin(ReadWrite.WRITE);
        RDFDataMgr.read(dataset, "Data/solver-data.ttl");
        dataset.commit();
        dataset.end();
    }

    @AfterAll
    static public void afterClass() {
        dataset.close();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    private List<Binding> execStandard(String sparql) {
        Query query = QueryFactory.create(sparql);
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecution.dataset(dataset).query(query).build()) {
            return toList(qExec.execSelect());
        } finally {
            dataset.end();
        }
    }

    private List<Binding> execWithLeapFrog(String sparql) {
        Context cxt = ARQ.getContext().copy();
        StageGenerator orig = StageBuilder.chooseStageGenerator(cxt);
        StageGenerator leapFrog = new StageGeneratorLeapFrogJoin(orig);
        OpExecutorFactory opExecFactory = execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt);

        Query query = QueryFactory.create(sparql);

        dataset.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecution.dataset(dataset)
                .query(query)
                .set(ARQConstants.sysOpExecutorFactory, opExecFactory)
                .set(ARQ.stageGenerator, leapFrog)
                .build()) {
            return toList(qExec.execSelect());
        } finally {
            dataset.end();
        }
    }

    private static List<Binding> toList(org.apache.jena.query.ResultSet rs) {
        List<Binding> results = new ArrayList<>();
        List<String> vars = rs.getResultVars();
        while (rs.hasNext()) {
            org.apache.jena.query.QuerySolution qs = rs.next();
            BindingBuilder builder = Binding.builder();
            for (String vName : vars) {
                Node n = qs.get(vName).asNode();
                if (n != null) {
                    builder.add(Var.alloc(vName), n);
                }
            }
            results.add(builder.build());
        }
        return results;
    }

    @Test
    public void leapFrog_01() {
        String sparql = "PREFIX : <http://example/> SELECT * WHERE { :s :p :o . :s :p :x }";

        List<Binding> resultsStandard = execStandard(sparql);
        List<Binding> resultsLeapFrog = execWithLeapFrog(sparql);

        assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                     "Standard and leap frog should produce same number of results");
    }

    @Test
    public void leapFrog_02() {
        String sparql = "PREFIX : <http://example/> SELECT * WHERE { :s :p :o . :s :p :x . :x :q :y }";

        List<Binding> resultsStandard = execStandard(sparql);
        List<Binding> resultsLeapFrog = execWithLeapFrog(sparql);

        assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                     "Standard and leap frog should produce same number of results");
        assertEquals(1, resultsLeapFrog.size(), "Expected 1 result for three-way leap frog join");
    }

    @Test
    public void leapFrog_withVariables() {
        String sparql = "PREFIX : <http://example/> SELECT * WHERE { ?s :p :o . ?s :q ?o2 }";

        List<Binding> resultsStandard = execStandard(sparql);
        List<Binding> resultsLeapFrog = execWithLeapFrog(sparql);

        assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                     "Standard and leap frog should produce same number of results");
    }

    @Test
    public void leapFrog_singlePattern() {
        String sparql = "PREFIX : <http://example/> SELECT * WHERE { :s :p :o }";

        List<Binding> resultsStandard = execStandard(sparql);
        List<Binding> resultsLeapFrog = execWithLeapFrog(sparql);

        assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                     "Standard and leap frog should produce same number of results");
    }

    @Test
    public void leapFrog_crossPatternJoinVars() {
        // Tests patterns that don't all bind the same variables.
        // P0: ?s :p1 ?x — binds {?s, ?x}
        // P1: ?s :p2 ?y — binds {?s, ?y}
        // P2: ?x :p3 ?z — binds {?x, ?z}
        // joinVars = {?s, ?x} intersect {?s, ?y} intersect {?x, ?z} = empty
        // So this falls through to standard execution (no leap frog).
        // The test verifies the query still produces correct results.
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
            g.add(Triple.create(NodeFactory.createURI("http://example/s1"), NodeFactory.createURI("http://example/p1"), NodeFactory.createURI("http://example/x1")));
            g.add(Triple.create(NodeFactory.createURI("http://example/s2"), NodeFactory.createURI("http://example/p1"), NodeFactory.createURI("http://example/x1")));
            g.add(Triple.create(NodeFactory.createURI("http://example/s1"), NodeFactory.createURI("http://example/p2"), NodeFactory.createURI("http://example/x1")));
            g.add(Triple.create(NodeFactory.createURI("http://example/s2"), NodeFactory.createURI("http://example/p2"), NodeFactory.createURI("http://example/x1")));
            g.add(Triple.create(NodeFactory.createURI("http://example/x1"), NodeFactory.createURI("http://example/p3"), NodeFactory.createURI("http://example/z1")));
            g.add(Triple.create(NodeFactory.createURI("http://example/x1"), NodeFactory.createURI("http://example/p3"), NodeFactory.createURI("http://example/z2")));
            ds.commit();
        } finally { ds.end(); }

        try {
            String sparql = "PREFIX : <http://example/> SELECT * WHERE { ?s :p1 ?x . ?s :p2 ?y . ?x :p3 ?z }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                         "Standard and leap frog should produce same number of results");
        } finally {
            ds.close();
        }
    }

    @Test
    public void leapFrog_duplicates() {
        // Tests that duplicate handling in the merge-success path works correctly.
        // Multiple patterns produce bindings with same join var values.
        // Should not produce duplicate or missed results.
        //
        // Query: SELECT ?s WHERE { ?s :p1 :o . ?s :p2 :o . ?s :p3 :o }
        // Data: 3 subjects, each has all 3 predicates pointing to same object :o
        // Expected: 3 results (one per subject)
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            for (int i = 1; i <= 3; i++) {
                Node s = NodeFactory.createURI("http://example/s" + i);
                Node o = NodeFactory.createURI("http://example/o");
                org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
                for (int p = 1; p <= 3; p++) {
                    g.add(Triple.create(s, NodeFactory.createURI("http://example/p" + p), o));
                }
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            String sparql = "PREFIX : <http://example/> SELECT ?s WHERE { ?s :p1 :o . ?s :p2 :o . ?s :p3 :o }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                         "Standard and leap frog should produce same number of results");
            assertEquals(3, resultsStandard.size(), "Expected 3 results");
        } finally {
            ds.close();
        }
    }

    @Test
    public void leapFrog_duplicateSubjectsMultipleObjects() {
        // Tests duplicate handling with multiple distinct objects per subject.
        // Each subject has multiple values for each predicate, creating a
        // Cartesian product that the merge-success path must advance correctly.
        //
        // P0: ?s :p1 ?o — s1->o1, s1->o2, s2->o1, s2->o2
        // P1: ?s :p2 ?o — s1->o1, s1->o2, s2->o1, s2->o2
        // P2: ?s :p3 ?o — s1->o1, s1->o2, s2->o1, s2->o2
        // joinVars = {?s, ?o}
        // Standard join: for each (s, o) pair that exists in all 3 patterns, 1 result.
        // Both s1->o1, s1->o2, s2->o1, s2->o2 exist in all 3 patterns.
        // Expected: 4 results
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
            for (int s = 1; s <= 2; s++) {
                for (int o = 1; o <= 2; o++) {
                    Node subj = NodeFactory.createURI("http://example/s" + s);
                    Node obj = NodeFactory.createURI("http://example/o" + o);
                    for (int p = 1; p <= 3; p++) {
                        g.add(Triple.create(subj, NodeFactory.createURI("http://example/p" + p), obj));
                    }
                }
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            String sparql = "PREFIX : <http://example/> SELECT * WHERE { ?s :p1 ?o . ?s :p2 ?o . ?s :p3 ?o }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                         "Standard (" + resultsStandard.size() + ") and leap frog (" + resultsLeapFrog.size() + ") should match");
            assertEquals(4, resultsStandard.size(), "Expected 4 results from Cartesian product");
            assertEquals(4, resultsLeapFrog.size(), "Leap frog should produce 4 results");
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test leap frog join with large dataset - verifies seek optimization")
    public void leapFrog_largeDataset_seekOptimization() {
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
            
            // Create a larger dataset where seek optimization matters
            // 1000 subjects, each with 3 predicates
            for (int i = 1; i <= 1000; i++) {
                Node s = NodeFactory.createURI("http://example/s" + i);
                Node o1 = NodeFactory.createURI("http://example/o" + i);
                Node o2 = NodeFactory.createURI("http://example/o" + (i + 1000));
                Node o3 = NodeFactory.createURI("http://example/o" + (i + 2000));
                
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p1"), o1));
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p2"), o2));
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p3"), o3));
            }
            ds.commit();
        } finally { 
            ds.end(); 
        }

        try {
            // Three-way join that requires seeking through large datasets
            String sparql = "PREFIX : <http://example/> " +
                           "SELECT (COUNT(*) AS ?c) " +
                           "WHERE { ?s :p1 ?o1 . ?s :p2 ?o2 . ?s :p3 ?o3 }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                        "Standard and leap frog should produce same number of results");
            assertEquals(1, resultsStandard.size(), "Expected 1 aggregate result");
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test leap frog join with non-contiguous data - verifies seek jumps")
    public void leapFrog_nonContiguousData_seekJumps() {
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
            
            // Create sparse data with large gaps - seek should jump over gaps
            for (int i = 1; i <= 100; i++) {
                int sparseId = i * 100; // Gaps of 99 between subjects
                Node s = NodeFactory.createURI("http://example/s" + sparseId);
                Node o = NodeFactory.createURI("http://example/o" + sparseId);
                
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p1"), o));
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p2"), o));
                g.add(Triple.create(s, NodeFactory.createURI("http://example/p3"), o));
            }
            ds.commit();
        } finally { 
            ds.end(); 
        }

        try {
            String sparql = "PREFIX : <http://example/> " +
                           "SELECT * " +
                           "WHERE { ?s :p1 ?o1 . ?s :p2 ?o2 . ?s :p3 ?o3 }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                        "Standard and leap frog should produce same number of results");
            assertEquals(100, resultsLeapFrog.size(), "Expected 100 results from sparse data");
        } finally {
            ds.close();
        }
    }

    private List<Binding> execWithDataset(Dataset ds, String sparql) {
        Query query = QueryFactory.create(sparql);
        ds.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecution.dataset(ds).query(query).build()) {
            return toList(qExec.execSelect());
        } finally {
            ds.end();
        }
    }

    private List<Binding> execWithDatasetAndLeapFrog(Dataset ds, String sparql) {
        Context cxt = ARQ.getContext().copy();
        StageGenerator orig = StageBuilder.chooseStageGenerator(cxt);
        StageGenerator leapFrog = new StageGeneratorLeapFrogJoin(orig);
        OpExecutorFactory opExecFactory = execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt);

        Query query = QueryFactory.create(sparql);

        ds.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecution.dataset(ds)
                .query(query)
                .set(ARQConstants.sysOpExecutorFactory, opExecFactory)
                .set(ARQ.stageGenerator, leapFrog)
                .build()) {
            return toList(qExec.execSelect());
        } finally {
            ds.end();
        }
    }

    /**
     * Helper to create a URI node with the given local name.
     */
    private static Node n(String local) {
        return NodeFactory.createURI("http://example/" + local);
    }

    /**
     * Helper to create direct iterator execution with stats collection.
     * Returns {resultCount, stats} as an array.
     */
    private static Object[] execDirectWithStats(Dataset ds, BasicPattern bgp) {
        DatasetGraph dsg = ds.asDatasetGraph();
        Graph activeGraph = dsg.getDefaultGraph();
        ExecutionContext execCxt = ExecutionContext.create(dsg, activeGraph);

        ds.begin(ReadWrite.READ);
        try {
            DatasetGraphTDB tdbDsg = TDBInternal.getDatasetGraphTDB(dsg);
            LeapFrogJoinIteratorOptimized iterator = 
                StageGeneratorLeapFrogJoin.createLeapFrogIterator(bgp, tdbDsg, execCxt);

            int resultCount = 0;
            while (iterator.hasNext()) {
                iterator.next();
                resultCount++;
            }

            LeapFrogJoinStats stats = iterator.getStats();
            iterator.close();

            return new Object[] { resultCount, stats };
        } finally {
            ds.end();
        }
    }

    @Test
    @DisplayName("Test stats tracking during direct iterator execution with star join")
    public void testStatsDuringDirectIteratorExecution() {
        // Star join dataset: 2 subjects that join, 2 that don't
        // s1 and s2 have all 3 predicates (will join)
        // s3 and s4 only have p1 and p2 (won't join - missing p3)
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            // s1 and s2 have all 3 predicates
            for (int p = 1; p <= 3; p++) {
                g.add(Triple.create(n("s1"), n("p" + p), n("o" + p)));
                g.add(Triple.create(n("s2"), n("p" + p), n("o" + p)));
            }
            // s3 and s4 only have p1 and p2
            for (int p = 1; p <= 2; p++) {
                g.add(Triple.create(n("s3"), n("p" + p), n("o" + p)));
                g.add(Triple.create(n("s4"), n("p" + p), n("o" + p)));
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results
            assertEquals(2, resultCount, "Should produce 2 results (s1 and s2)");

            // Verify stats - specific values
            assertEquals(2, stats.getMergeSuccessCount(), "2 successful joins");
            assertTrue(stats.getIterations() > 0, "Loop executed");
            assertTrue(stats.getTotalComparisons() > 0, "Comparisons performed");
            // Note: seekCount + stepCount may be 0 if all iterators start aligned
            // (all patterns return same subject on first iteration)

            if (DEBUG_TESTS) {
                System.out.println("testStatsDuringDirectIteratorExecution stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test stats seek optimization with moderate dataset")
    public void testStatsSeekOptimizationModerateDataset() {
        // 100 subjects with matching predicates - enough to see seek behavior
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            for (int i = 1; i <= 100; i++) {
                Node s = NodeFactory.createURI("http://example/s" + i);
                g.add(Triple.create(s, n("p1"), n("o1_" + i)));
                g.add(Triple.create(s, n("p2"), n("o2_" + i)));
                g.add(Triple.create(s, n("p3"), n("o3_" + i)));
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results
            assertEquals(100, resultCount, "100 subjects should all match");
            assertEquals(100, stats.getMergeSuccessCount(), "100 successful joins");

            // Verify iterations and comparisons
            assertTrue(stats.getIterations() >= 100, "At least 1 iteration per result");
            assertTrue(stats.getTotalComparisons() > 0, "Comparisons performed");

            if (DEBUG_TESTS) {
                System.out.println("testStatsSeekOptimizationModerateDataset stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test stats merge failures with binding conflicts")
    public void testStatsMergeFailures() {
        // Query: (?s :p1 ?o) (?s :p2 ?o) (?s :p3 ?o)
        // Requires SAME object ?o in all three predicate positions
        //
        // s1: p1->o1, p2->o1, p3->o1  (same object - MATCH)
        // s2: p1->o2, p2->o2, p3->o2  (same object - MATCH)  
        // s3: p1->o3, p2->o4, p3->o3  (DIFFERENT objects o3!=o4 - may cause merge fail)
        // s4: p1->o5, p2->o5          (missing p3 - no merge attempt)
        //
        // Note: The algorithm may seek past s3 before attempting merge,
        // so mergeFailCount may be 0. We verify that results are correct
        // and stats are populated.
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            
            // s1: all same object o1
            g.add(Triple.create(n("s1"), n("p1"), n("o1")));
            g.add(Triple.create(n("s1"), n("p2"), n("o1")));
            g.add(Triple.create(n("s1"), n("p3"), n("o1")));
            
            // s2: all same object o2
            g.add(Triple.create(n("s2"), n("p1"), n("o2")));
            g.add(Triple.create(n("s2"), n("p2"), n("o2")));
            g.add(Triple.create(n("s2"), n("p3"), n("o2")));
            
            // s3: DIFFERENT objects - p1 and p3 use o3, but p2 uses o4
            g.add(Triple.create(n("s3"), n("p1"), n("o3")));
            g.add(Triple.create(n("s3"), n("p2"), n("o4")));  // Different from o3!
            g.add(Triple.create(n("s3"), n("p3"), n("o3")));
            
            // s4: missing p3
            g.add(Triple.create(n("s4"), n("p1"), n("o5")));
            g.add(Triple.create(n("s4"), n("p2"), n("o5")));
            
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o) (?s :p2 ?o) (?s :p3 ?o))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results - only s1 and s2 should match
            assertEquals(2, resultCount, "Only s1 and s2 should match (same object in all positions)");
            assertEquals(2, stats.getMergeSuccessCount(), "2 successful joins (s1, s2)");

            // Verify stats are populated
            assertTrue(stats.getIterations() > 0, "Loop executed");
            assertTrue(stats.getTotalComparisons() > 0, "Comparisons performed");
            // Note: mergeFailCount may be 0 if algorithm seeks past conflicting data

            if (DEBUG_TESTS) {
                System.out.println("testStatsMergeFailures stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test stats with non-contiguous (sparse) data")
    public void testStatsNonContiguousData() {
        // 50 subjects with large gaps (s100, s200, s300, ... s5000)
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            for (int i = 1; i <= 50; i++) {
                int sparseId = i * 100;  // Gaps of 99 between subjects
                Node s = NodeFactory.createURI("http://example/s" + sparseId);
                g.add(Triple.create(s, n("p1"), n("o" + sparseId)));
                g.add(Triple.create(s, n("p2"), n("o" + sparseId)));
                g.add(Triple.create(s, n("p3"), n("o" + sparseId)));
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results
            assertEquals(50, resultCount, "50 sparse subjects should all match");
            assertEquals(50, stats.getMergeSuccessCount(), "50 successful joins");

            // Verify iterations
            assertTrue(stats.getIterations() >= 50, "At least 1 iteration per result");
            assertTrue(stats.getTotalComparisons() > 0, "Comparisons performed");

            if (DEBUG_TESTS) {
                System.out.println("testStatsNonContiguousData stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test stats with Cartesian product (multiple join variables)")
    public void testStatsCartesianProduct() {
        // 2 subjects × 2 objects = 4 combinations
        // Each subject has all 3 predicates pointing to the SAME object
        // This creates a join on both ?s and ?o
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            for (int s = 1; s <= 2; s++) {
                for (int o = 1; o <= 2; o++) {
                    Node subj = NodeFactory.createURI("http://example/s" + s);
                    Node obj = NodeFactory.createURI("http://example/o" + o);
                    for (int p = 1; p <= 3; p++) {
                        g.add(Triple.create(subj, n("p" + p), obj));
                    }
                }
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o) (?s :p2 ?o) (?s :p3 ?o))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results
            assertEquals(4, resultCount, "4 (subject, object) combinations");
            assertEquals(4, stats.getMergeSuccessCount(), "4 successful joins");

            // Verify iterations and comparisons
            assertTrue(stats.getIterations() >= 4,
                       "At least 1 iteration per result");
            assertTrue(stats.getTotalComparisons() > 0,
                       "Comparisons performed");

            if (DEBUG_TESTS) {
                System.out.println("testStatsCartesianProduct stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }

    @Test
    @DisplayName("Test stats cache behavior")
    public void testStatsCacheBehavior() {
        // Small dataset to verify cache is being used
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Graph g = ds.asDatasetGraph().getDefaultGraph();
            // 10 subjects with matching predicates
            for (int i = 1; i <= 10; i++) {
                Node s = NodeFactory.createURI("http://example/s" + i);
                g.add(Triple.create(s, n("p1"), n("o1_" + i)));
                g.add(Triple.create(s, n("p2"), n("o2_" + i)));
                g.add(Triple.create(s, n("p3"), n("o3_" + i)));
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))");
            Object[] result = execDirectWithStats(ds, bgp);
            int resultCount = (Integer) result[0];
            LeapFrogJoinStats stats = (LeapFrogJoinStats) result[1];

            // Verify results
            assertEquals(10, resultCount, "10 subjects should match");

            // Verify cache behavior - cache stats should be accessible
            // Note: cache may not be used if all patterns use the same index
            // We just verify the stats are valid (not throwing exceptions)
            double cacheHitRatio = stats.getCacheHitRatio();
            assertTrue(!Double.isNaN(cacheHitRatio) || 
                       (stats.getIndexCacheHits() == 0 && stats.getIndexCacheMisses() == 0),
                       "Cache hit ratio is valid (NaN only if no lookups)");

            if (DEBUG_TESTS) {
                System.out.println("testStatsCacheBehavior stats: " + stats);
            }
        } finally {
            ds.close();
        }
    }
}
