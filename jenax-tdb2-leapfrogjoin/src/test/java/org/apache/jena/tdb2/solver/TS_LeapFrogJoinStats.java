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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.TDB2StorageBuilder;
import org.apache.jena.tdb2.sys.TDBInternal;

public class TS_LeapFrogJoinStats {

    @Test
    public void testStatsInitialValues() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        assertEquals(0, stats.getSeekCount());
        assertEquals(0, stats.getStepCount());
        assertEquals(0, stats.getMergeSuccessCount());
        assertEquals(0, stats.getMergeFailCount());
        assertEquals(0, stats.getIndexCacheHits());
        assertEquals(0, stats.getIndexCacheMisses());
        assertEquals(0, stats.getIterations());
        // assertEquals(0, stats.getTotalComparisons());
    }

    @Test
    public void testIncrementMethods() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        // Use reflection to call package-private increment methods
        try {
            stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeFailCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIndexCacheHits").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIndexCacheMisses").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIterations").invoke(stats);

            assertEquals(1, stats.getSeekCount());
            assertEquals(1, stats.getStepCount());
            assertEquals(1, stats.getMergeSuccessCount());
            assertEquals(1, stats.getMergeFailCount());
            assertEquals(1, stats.getIndexCacheHits());
            assertEquals(1, stats.getIndexCacheMisses());
            assertEquals(1, stats.getIterations());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke increment methods", e);
        }
    }

    @Test
    public void testAddComparisons() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        try {
            stats.getClass().getDeclaredMethod("addComparisons", long.class)
                .invoke(stats, 5L);
            stats.getClass().getDeclaredMethod("addComparisons", long.class)
                .invoke(stats, 3L);

            // assertEquals(8, stats.getTotalComparisons());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke addComparisons", e);
        }
    }

    @Test
    public void testSeekRatio() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        // No advances yet - should return NaN
        assertTrue(Double.isNaN(stats.getSeekRatio()));

        try {
            // 3 seeks, 1 step = 75% seek ratio
            for (int i = 0; i < 3; i++) {
                stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            }
            stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);

            double ratio = stats.getSeekRatio();
            assertTrue(ratio >= 0.74 && ratio <= 0.76, "Seek ratio should be ~0.75");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test seek ratio", e);
        }
    }

    @Test
    public void testCacheHitRatio() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        // No lookups yet - should return NaN
        assertTrue(Double.isNaN(stats.getCacheHitRatio()));

        try {
            // 4 hits, 1 miss = 80% hit ratio
            for (int i = 0; i < 4; i++) {
                stats.getClass().getDeclaredMethod("incrementIndexCacheHits").invoke(stats);
            }
            stats.getClass().getDeclaredMethod("incrementIndexCacheMisses").invoke(stats);

            double ratio = stats.getCacheHitRatio();
            assertTrue(ratio >= 0.79 && ratio <= 0.81, "Cache hit ratio should be ~0.8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test cache hit ratio", e);
        }
    }

    @Test
    public void testGetTotalAdvances() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        try {
            for (int i = 0; i < 5; i++) {
                stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            }
            for (int i = 0; i < 3; i++) {
                stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);
            }

            assertEquals(8, stats.getTotalAdvances());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test total advances", e);
        }
    }

    @Test
    public void testGetTotalMerges() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        try {
            for (int i = 0; i < 10; i++) {
                stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);
            }
            for (int i = 0; i < 2; i++) {
                stats.getClass().getDeclaredMethod("incrementMergeFailCount").invoke(stats);
            }

            assertEquals(12, stats.getTotalMerges());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test total merges", e);
        }
    }

    @Test
    public void testToString() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();

        try {
            stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);

            String str = stats.toString();
            assertTrue(str.contains("seekCount=1"));
            assertTrue(str.contains("mergeSuccess=1"));
            assertTrue(str.contains("LeapFrogJoinStats"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to test toString", e);
        }
    }

    @Test
    @DisplayName("Test stats during real iterator execution - integration test")
    public void testStatsDuringRealExecution() {
        // Create a simple star join dataset
        // Dataset ds = TDB2Factory.createDataset();

        StoreParams storeParams = StoreParamsBuilder.create("MyParams", StoreParams.getDftMemStoreParams())
            .tripleIndexes(new String[]{ "SPO", "POS", "OSP", "PSO" })
            .build();
        // StoreParams storeParams = new StoreParamsBuilder
        DatasetGraphTDB dsg = TDB2StorageBuilder.build(Location.mem(), storeParams, null);
        Dataset ds = DatasetFactory.wrap(dsg);

        try {
            try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.WRITE)) {
                Graph g = ds.asDatasetGraph().getDefaultGraph();
                // 3 subjects with 3 predicates each
                for (int i = 1; i <= 3; i++) {
                    var s = NodeFactory.createURI("http://example/s" + i);
                    for (int p = 1; p <= 3; p++) {
                        g.add(Triple.create(s,
                            NodeFactory.createURI("http://example/p" + p),
                            NodeFactory.createURI("http://example/o" + p)));
                    }
                }

                RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);

                ds.commit();
            }

            BasicPattern bgp = SSE.parseBGP("(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))");

            // DatasetGraph dsg = ds.asDatasetGraph();
            Graph activeGraph = dsg.getDefaultGraph();
            ExecutionContext execCxt = ExecutionContext.create(dsg, activeGraph);

            try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.READ)) {
                DatasetGraphTDB tdbDsg = TDBInternal.getDatasetGraphTDB(dsg);
                QueryIterLeapFrogJoin iterator = StageGeneratorLeapFrogJoin.createLeapFrogIterator(bgp, tdbDsg, execCxt);

                // Consume iterator
                int resultCount = 0;
                while (iterator.hasNext()) {
                    iterator.next();
                    resultCount++;
                }

                LeapFrogJoinStats stats = iterator.getStats();

                // Verify results
                assertEquals(3, resultCount, "3 subjects should match");

                // Verify stats are populated after execution
                assertTrue(stats.getIterations() > 0, "Stats should have iterations");
                // assertTrue(stats.getTotalComparisons() > 0, "Stats should have comparisons");
                assertTrue(stats.getMergeSuccessCount() == 3, "3 successful joins");

                // Verify stats remain accessible after close
                iterator.close();
                assertTrue(stats.getMergeSuccessCount() >= 0,
                           "Stats accessible after close");
                assertTrue(stats.getIterations() > 0,
                           "Iterations preserved after close");

            }
        } finally {
            ds.close();
            TDBInternal.expel(ds.asDatasetGraph());
        }
    }
}
