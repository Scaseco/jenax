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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.query.ARQ;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;

public class LeapFrogDebugTest {

    public static void main(String[] args) {
        System.out.println("Leap Frog Debug Test");
        System.out.println("=====================\n");

        Dataset dataset = TDB2Factory.createDataset();
        try {
            generateTestData(dataset.asDatasetGraph(), 3);

            String queryStr = "PREFIX : <http://example/> SELECT (count(*) AS ?c) { ?s :p1 ?o1 ; :p2 ?o2 ; :p3 ?o3 }";
            Query query = QueryFactory.create(queryStr);

            System.out.println("Running standard execution.");
            try (AutoTxn txn = Txn.autoTxn(dataset, ReadWrite.READ)) {
                try (QueryExecution qExec = QueryExecution.dataset(dataset).query(query).build()) {
                    System.out.println(queryStr);
                    System.out.println(org.apache.jena.query.ResultSetFormatter.asText(qExec.execSelect()));
                }
            }

            System.out.println("\nRunning leap frog join execution.");
            Context cxt = ARQ.getContext().copy();
            StageGenerator orig = StageBuilder.chooseStageGenerator(cxt);
            StageGenerator leapFrog = new StageGeneratorLeapFrogJoin(orig);
            OpExecutorFactory opExecFactory = execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt);
            
            try (AutoTxn txn = Txn.autoTxn(dataset, ReadWrite.READ)) {
                try (QueryExecution qExec = QueryExecution.dataset(dataset)
                        .query(query)
                        .set(ARQConstants.sysOpExecutorFactory, opExecFactory)
                        .set(ARQ.stageGenerator, leapFrog)
                        .build()) {
                    System.out.println(queryStr);
                    System.out.println(org.apache.jena.query.ResultSetFormatter.asText(qExec.execSelect()));
                }
            }
        } finally {
            dataset.close();
        }
    }

    private static void generateTestData(org.apache.jena.sparql.core.DatasetGraph dsg, int count) {
        try (AutoTxn txn = Txn.autoTxn(dsg, ReadWrite.WRITE)) {
            Graph defaultGraph = dsg.getDefaultGraph();

            for (int i = 1; i <= count; i++) {
                Node subject = NodeFactory.createURI("http://example/s" + i);
                Node object = NodeFactory.createURI("http://example/o" + i);

                defaultGraph.add(Triple.create(subject, NodeFactory.createURI("http://example/p1"), object));
                defaultGraph.add(Triple.create(subject, NodeFactory.createURI("http://example/p2"), object));
                defaultGraph.add(Triple.create(subject, NodeFactory.createURI("http://example/p3"), object));
            }
            txn.commit();
        }
    }
}
