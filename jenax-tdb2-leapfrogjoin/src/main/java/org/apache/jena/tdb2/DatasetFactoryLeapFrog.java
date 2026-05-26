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

package org.apache.jena.tdb2;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.tdb2.solver.OpExecutorTDB2WithForcedStageGenerator;
import org.apache.jena.tdb2.solver.StageGeneratorLeapFrogJoin;

/**
 * Factory for creating TDB2 datasets with leap frog join enabled.
 * <p>
 * This class provides a simple way to enable the leap frog join optimization
 * in TDB2 datasets. Leap frog join is a query execution strategy that
 * coordinates multiple sorted iterators to efficiently join basic graph
 * patterns by leveraging the sorted nature of TDB2's B+Tree indices.
 * <p>
 * Usage example:
 * <pre>
 *     Dataset ds = DatasetFactoryLeapFrog.create();
 *     ds.begin(ReadWrite.ReadWrite);
 *     // ... execute SPARQL queries with leap frog join enabled ...
 *     ds.commit();
 *     ds.close();
 * </pre>
 * <p>
 * The leap frog join is automatically activated for multi-triple patterns
 * where shared variables exist between patterns. It is transparent to users
 * — queries work the same way, just potentially faster.
 */
public class DatasetFactoryLeapFrog {

    private DatasetFactoryLeapFrog() {}
    
    /**
     * Create a TDB2 dataset with leap frog join enabled.
     * <p>
     * This method creates a standard TDB2 dataset and configures it to use
     * the leap frog join stage generator. The leap frog optimization is
     * applied automatically for compatible query patterns.
     *
     * @return a TDB2 dataset with leap frog join enabled
     */
    public static Dataset create() {
        Dataset ds = TDB2Factory.createDataset();
        StageGenerator orig = StageBuilder.chooseStageGenerator(ds.getContext());
        StageGenerator leapFrog = new StageGeneratorLeapFrogJoin(orig);
        ds.getContext().set(ARQ.stageGenerator, leapFrog);
        ds.getContext().set(ARQConstants.sysOpExecutorFactory, (OpExecutorFactory) execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt));
        return ds;
    }
}
