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
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;

// Issue: TDB2 doesn't use the stage generator:
// QueryIterator.execute(OpQuadPattern, ...) is overridden
public class LeapFrogJoinBenchmark {

    private static final int DEFAULT_WARMUP_ITERATIONS = 3;
    private static final int DEFAULT_MEASURED_ITERATIONS = 5;

    public static void main(String[] args) {
        System.out.println("Leap Frog Join Benchmark");
        System.out.println("========================\n");

        int warmupIters = DEFAULT_WARMUP_ITERATIONS;
        int measuredIters = DEFAULT_MEASURED_ITERATIONS;

        if (args.length >= 1) {
            warmupIters = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            measuredIters = Integer.parseInt(args[1]);
        }

        LeapFrogJoinBenchmark benchmark = new LeapFrogJoinBenchmark();

        benchmark.runBenchmark(new int[] {10_000, 100, 10_000}, warmupIters, measuredIters);
    }

    public void runBenchmark(int[] pInstances, int warmupIterations, int measuredIterations) {
        Dataset dataset = TDB2Factory.createDataset();
        try {
            generateTestData(dataset.asDatasetGraph(), pInstances);

            String queryStr = "SELECT (count(*) AS ?c) { ?s <http://example/p1> ?o1 ; <http://example/p2> ?o2 ; <http://example/p3> ?o3 }";
            Query query = QueryFactory.create(queryStr);

            System.out.println("Running standard execution.");
            double standardTime = 0;
            {
                StageGenerator stageGen = StageBuilder.chooseStageGenerator(null);
                OpExecutorFactory opExecFactory = QC.getFactory(dataset.getContext());
                standardTime = runWithWarmup(dataset, query, opExecFactory, stageGen, warmupIterations, measuredIterations);
            }

            System.out.println("Running leap frog join execution.");
            double leapFrogTime = 0;
            {
                StageGenerator stageGen = new StageGeneratorLeapFrogJoin(StageBuilder.chooseStageGenerator(null));
                OpExecutorFactory opExecFactory = execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt);
                leapFrogTime = runWithWarmup(dataset, query, opExecFactory, stageGen, warmupIterations, measuredIterations);
            }
            System.out.printf("Warmup: %d iterations (discarded), Measured: %d iterations\n", warmupIterations, measuredIterations);
            System.out.printf("Standard: %.3f ms, Leap Frog: %.3f ms, Speedup: %.2fx\n",
                standardTime, leapFrogTime, standardTime / leapFrogTime);
        } finally {
            dataset.close();
        }
    }

    private double runWithWarmup(Dataset dataset, Query query, OpExecutorFactory opExecFactory, StageGenerator stageGen,
                                  int warmupIterations, int measuredIterations) {
        if (opExecFactory == null) {
            opExecFactory = QC.getFactory(dataset.getContext());
        }
        if (stageGen == null) {
            stageGen = StageBuilder.chooseStageGenerator(dataset.getContext());
        }

        // Warmup: run without timing to let JVM JIT, caches, and index structures stabilize
        System.out.println("  Warmup: " + warmupIterations + " iterations (discarded)...");
        for (int i = 0; i < warmupIterations; i++) {
            try (AutoTxn txn = Txn.autoTxn(dataset, ReadWrite.READ)) {
                try (QueryExecution qExec = QueryExecution
                        .dataset(dataset)
                        .query(query)
                        .set(ARQConstants.sysOpExecutorFactory, opExecFactory)
                        .set(ARQ.stageGenerator, stageGen)
                        .build()) {
                    ResultSet rs = qExec.execSelect();
                    ResultSetFormatter.consume(rs);
                }
                txn.commit();
            }
        }

        // Measured: run and time
        double totalTime = 0;
        for (int i = 0; i < measuredIterations; i++) {
            try (AutoTxn txn = Txn.autoTxn(dataset, ReadWrite.READ)) {
                try (QueryExecution qExec = QueryExecution
                        .dataset(dataset)
                        .query(query)
                        .set(ARQConstants.sysOpExecutorFactory, opExecFactory)
                        .set(ARQ.stageGenerator, stageGen)
                        .build()) {
                    long start = System.nanoTime();
                    ResultSet rs = qExec.execSelect();
                    ResultSetFormatter.consume(rs);
                    long end = System.nanoTime();
                    totalTime += (end - start) / 1_000_000.0;
                }
                txn.commit();
            }
        }
        return totalTime / measuredIterations;
    }

    private static void generateTestData(DatasetGraph dsg, int[] pInstances) {
        try (AutoTxn txn = Txn.autoTxn(dsg, ReadWrite.WRITE)) {
            Graph defaultGraph = dsg.getDefaultGraph();

            for (int k = 0; k < pInstances.length; ++k) {
                Node p = NodeFactory.createURI("http://example/p" + (k + 1));

                int j = pInstances[k];
                for (int i = 1; i <= j; i++) {
                    Node subject = NodeFactory.createURI("http://example/s" + i);
                    Node object1 = NodeFactory.createURI("http://example/o" + i);
                    defaultGraph.add(Triple.create(subject, p, object1));
                }
            }
            txn.commit();
        }
    }
}
