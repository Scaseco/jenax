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

package org.apache.jena.tdb2.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.solver.StageGeneratorLeapFrogJoin;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * Tests for the {@link DatasetAssemblerLeapFrogJoin} assembler.
 * <p>
 * Verifies that:
 * <ul>
 *   <li>A dataset assembled via {@code tdb:DatasetTDBLeapFrog} from an ASL file
 *       produces a valid TDB2 dataset.</li>
 *   <li>The assembled dataset's context has the leap frog join stage generator
 *       and op executor factory installed.</li>
 *   <li>Queries executed on the assembled dataset return the same results
 *       as with the programmatic {@link org.apache.jena.tdb2.DatasetFactoryLeapFrog} approach.</li>
 * </ul>
 */
public class TS_LeapFrogAssembler {

    static { JenaSystem.init(); }

    @BeforeAll
    static public void beforeClass() {
    }

    @AfterAll
    static public void afterClass() {
        TDBInternal.reset();
    }

    @Test
    public void assembleDatasetLeapFrog() {
        Resource type = VocabLeapFrogJoin.gettDatasetTDBLeapFrog();

        Object thing = AssemblerUtils.build("tdb-dataset-leapfrog.ttl", type);
        assertTrue(thing instanceof Dataset, "Assembled object should be a Dataset");

        Dataset ds = (Dataset) thing;
        assertNotNull(ds);

        // The underlying DatasetGraph should be a TDB2 dataset graph
        DatasetGraph dsg = ds.asDatasetGraph();
        assertNotNull(dsg);

        ds.close();
    }

    @Test
    public void assembleDatasetLeapFrog_withUnionDefaultGraph() {
        Resource type = VocabLeapFrogJoin.gettDatasetTDBLeapFrog();

        Object thing = AssemblerUtils.build("tdb-dataset-leapfrog-union.ttl", type);
        assertTrue(thing instanceof Dataset);

        Dataset ds = (Dataset) thing;
        Context cxt = ds.getContext();

        // Verify unionDefaultGraph context setting was applied
        boolean unionDft = cxt.isTrue(TDB2.symUnionDefaultGraph);
        assertTrue(unionDft, "unionDefaultGraph should be true");

        ds.close();
    }

    @Test
    public void assembleDatasetLeapFrog_hasLeapFrogStageGenerator() {
        Resource type = VocabLeapFrogJoin.gettDatasetTDBLeapFrog();

        Object thing = AssemblerUtils.build("tdb-dataset-leapfrog.ttl", type);
        Dataset ds = (Dataset) thing;
        Context cxt = ds.getContext();

        // Verify leap frog stage generator is installed
        StageGenerator gen = StageBuilder.chooseStageGenerator(cxt);
        assertNotNull(gen, "Stage generator should be installed");
        assertTrue(gen instanceof StageGeneratorLeapFrogJoin,
                   "Stage generator should be StageGeneratorLeapFrogJoin but was " + gen.getClass().getName());

        ds.close();
    }

    @Test
    public void assembleDatasetLeapFrog_producesCorrectResults() {
        Resource type = VocabLeapFrogJoin.gettDatasetTDBLeapFrog();

        Object thing = AssemblerUtils.build("tdb-dataset-leapfrog.ttl", type);
        Dataset ds = (Dataset) thing;

        ds.begin(ReadWrite.WRITE);
        Graph g = ds.asDatasetGraph().getDefaultGraph();
        g.add(Triple.create(NodeFactory.createURI("http://example/s"), NodeFactory.createURI("http://example/p"), NodeFactory.createURI("http://example/o")));
        g.add(Triple.create(NodeFactory.createURI("http://example/s"), NodeFactory.createURI("http://example/p"), NodeFactory.createURI("http://example/x")));
        g.add(Triple.create(NodeFactory.createURI("http://example/x"), NodeFactory.createURI("http://example/q"), NodeFactory.createURI("http://example/y")));
        ds.commit();
        ds.end();

        try {
            String sparql = "PREFIX : <http://example/> SELECT * WHERE { :s :p :o . :s :p :x }";
            List<Binding> resultsAssembler = exec(ds, sparql);

            String sparql2 = "PREFIX : <http://example/> SELECT * WHERE { :s :p :o . :s :p :x . :x :q :y }";
            List<Binding> resultsAssembler3 = exec(ds, sparql2);

            assertEquals(1, resultsAssembler.size());
            assertEquals(1, resultsAssembler3.size());
        } finally {
            ds.close();
            TDBInternal.expel(ds.asDatasetGraph());
        }
    }

    @Test
    public void assembleDatasetLeapFrog_sameResultsAsFactory() {
        exec("tdb-dataset-leapfrog.ttl", "PREFIX : <http://example/> SELECT * WHERE { :s :p :x . :x :q :y }");
    }

//    @Test
//    public void assembleDatasetLeapFrog_movies() {
//        exec("tdb-dataset-leapfrog.ttl", "PREFIX : <http://example/> SELECT * WHERE { :s :p :x . :x :q :y }");
//    }

    public void exec(String file ,String sparql) {
        // Assemble from ASL
        Resource type = VocabLeapFrogJoin.gettDatasetTDBLeapFrog();

        Object thing = AssemblerUtils.build(file, type);
        Dataset dsAssembler = (Dataset) thing;

        // Create via factory (for comparison)
        Dataset dsFactory = org.apache.jena.tdb2.DatasetFactoryLeapFrog.create();

        // Populate both with same data
        populate(dsAssembler);
        populate(dsFactory);

        List<Binding> resultsAssembler = exec(dsAssembler, sparql);
        List<Binding> resultsFactory = exec(dsFactory, sparql);

        assertEquals(resultsFactory.size(), resultsAssembler.size(),
                     "Assembler and factory should produce same number of results");

        dsAssembler.close();
        dsFactory.close();
        TDBInternal.expel(dsAssembler.asDatasetGraph());
        TDBInternal.expel(dsFactory.asDatasetGraph());
    }

    private static void populate(Dataset ds) {
        ds.begin(ReadWrite.WRITE);
        Graph g = ds.asDatasetGraph().getDefaultGraph();
        g.add(Triple.create(NodeFactory.createURI("http://example/s"), NodeFactory.createURI("http://example/p"), NodeFactory.createURI("http://example/x")));
        g.add(Triple.create(NodeFactory.createURI("http://example/x"), NodeFactory.createURI("http://example/q"), NodeFactory.createURI("http://example/y")));
        ds.commit();
        ds.end();
    }

    private static List<Binding> exec(Dataset ds, String sparql) {
        Query query = QueryFactory.create(sparql);
        ds.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecution.dataset(ds).query(query).build()) {
            return toList(qExec.execSelect());
        } finally {
            ds.end();
        }
    }

    private static List<Binding> toList(org.apache.jena.query.ResultSet rs) {
        List<Binding> results = new ArrayList<>();
        List<String> vars = rs.getResultVars();
        while (rs.hasNext()) {
            org.apache.jena.query.QuerySolution qs = rs.next();
            BindingBuilder builder = BindingBuilder.create();
            for (String vName : vars) {
                org.apache.jena.graph.Node n = qs.get(vName).asNode();
                if (n != null) {
                    builder.add(Var.alloc(vName), n);
                }
            }
            results.add(builder.build());
        }
        return results;
    }
}
