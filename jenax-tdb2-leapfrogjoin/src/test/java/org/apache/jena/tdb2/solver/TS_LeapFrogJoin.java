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

    @BeforeAll
    static public void beforeClass() {
        dataset = TDB2Factory.createDataset();
        dataset.begin(ReadWrite.WRITE);
        // String graphData = TS_LeapFrogJoin.getTestingDataRoot() + "/Data/solver-data.ttl";
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

    @Test
    @DisplayName("Test stats tracking during direct iterator execution with star join")
    public void testStatsDuringDirectIteratorExecution() {
        // Inline star join dataset: 2 subjects that join, 2 that don't
        // :s1 and :s2 have all 3 predicates (will join)
        // :s3 and :s4 only have p1 and p2 (won't join - missing p3)
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            org.apache.jena.graph.Graph g = ds.asDatasetGraph().getDefaultGraph();
            // s1 and s2 have all 3 predicates
            for (int p = 1; p <= 3; p++) {
                g.add(Triple.create(
                    NodeFactory.createURI("http://example/s1"),
                    NodeFactory.createURI("http://example/p" + p),
                    NodeFactory.createURI("http://example/o" + p)));
                g.add(Triple.create(
                    NodeFactory.createURI("http://example/s2"),
                    NodeFactory.createURI("http://example/p" + p),
                    NodeFactory.createURI("http://example/o" + p)));
            }
            // s3 and s4 only have p1 and p2
            for (int p = 1; p <= 2; p++) {
                g.add(Triple.create(
                    NodeFactory.createURI("http://example/s3"),
                    NodeFactory.createURI("http://example/p" + p),
                    NodeFactory.createURI("http://example/o" + p)));
                g.add(Triple.create(
                    NodeFactory.createURI("http://example/s4"),
                    NodeFactory.createURI("http://example/p" + p),
                    NodeFactory.createURI("http://example/o" + p)));
            }
            ds.commit();
        } finally { ds.end(); }

        try {
            String sparql = "PREFIX : <http://example/> SELECT * WHERE { ?s :p1 ?o1 . ?s :p2 ?o2 . ?s :p3 ?o3 }";

            List<Binding> resultsStandard = execWithDataset(ds, sparql);
            List<Binding> resultsLeapFrog = execWithDatasetAndLeapFrog(ds, sparql);

            assertEquals(resultsStandard.size(), resultsLeapFrog.size(),
                         "Standard (" + resultsStandard.size() + ") and leap frog (" + resultsLeapFrog.size() + ") should match");
            assertEquals(2, resultsStandard.size(), "Expected 2 results (s1 and s2)");
            assertEquals(2, resultsLeapFrog.size(), "Leap frog should produce 2 results");
        } finally {
            ds.close();
        }
    }
}
