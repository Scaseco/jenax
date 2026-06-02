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

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.solver.OpExecutorTDB2WithForcedStageGenerator;
import org.apache.jena.tdb2.solver.StageGeneratorLeapFrogJoin;

/**
 * Assembler for TDB2 datasets with leap frog join optimization enabled.
 * <p>
 * This assembler extends {@link DatasetAssemblerTDB2} to create a TDB2 dataset
 * (from a {@code tdb:location} and optional {@code tdb:unionDefaultGraph})
 * and then configures the dataset's context to use the leap frog join
 * stage generator and the corresponding op executor.
 * <p>
 * Usage in an assembler description file:
 * <pre>{@code
 * <#dataset> rdf:type tdb:DatasetTDBLeapFrog ;
 *     tdb:location "/path/to/store" ;
 *     tdb:unionDefaultGraph true .
 * }</pre>
 * <p>
 * The leap frog join is applied automatically for compatible query patterns
 * (BGP with 2+ triples and shared variables). Incompatible patterns fall
 * back to standard TDB2 execution transparently.
 */
public class DatasetAssemblerLeapFrogJoin extends DatasetAssemblerTDB2 {

    static { JenaSystem.init(); }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        // Build the base TDB2 dataset (location, unionDefaultGraph, context merge)
        DatasetGraph dsg = super.createDataset(a, root);

        // Install leap frog join configuration on the dataset's context
        StageGenerator orig = StageBuilder.chooseStageGenerator(dsg.getContext());
        dsg.getContext().set(ARQ.stageGenerator, new StageGeneratorLeapFrogJoin(orig));
        dsg.getContext().set(ARQConstants.sysOpExecutorFactory,
            (OpExecutorFactory) execCxt -> new OpExecutorTDB2WithForcedStageGenerator(execCxt));

        return dsg;
    }
}
