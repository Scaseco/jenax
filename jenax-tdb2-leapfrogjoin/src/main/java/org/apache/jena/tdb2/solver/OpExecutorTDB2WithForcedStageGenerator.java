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

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterDistinguishedVars;

/**
 * OpExecutorTDB2 variant that forces execution through the stage generator.
 * <p>
 * The default {@link OpExecutorTDB2} bypasses the {@code stageGenerator} by directly
 * calling {@code executeBGP(OpBGP)}. This subclass overrides that path so that
 * all BGP execution goes through the registered {@code stageGenerator}, enabling
 * custom stage generators (such as {@link StageGeneratorLeapFrogJoin}) to intercept
 * and optimize query execution.
 * <p>
 * This is the key class that makes the leap frog join opt-in: it must be installed
 * as the {@code opExecutorFactory} in the {@link org.apache.jena.tdb2.DatasetFactoryLeapFrog}
 * context.
 */
public class OpExecutorTDB2WithForcedStageGenerator extends OpExecutorTDB2
{
    private final boolean hideBNodeVars;

    public OpExecutorTDB2WithForcedStageGenerator(ExecutionContext execCxt) {
        super(execCxt);
        this.hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables);
    }

    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
        BasicPattern pattern = opBGP.getPattern();
        QueryIterator qIter = stageGenerator.execute(pattern, input, execCxt);
        if ( hideBNodeVars )
            qIter = new QueryIterDistinguishedVars(qIter, execCxt);
        return qIter;
    }

    @Override
    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input) {
        // Convert to BGP forms to execute in this graph-centric engine.
        if ( quadPattern.isDefaultGraph() && execCxt.getActiveGraph() == execCxt.getDataset().getDefaultGraph() ) {
            // Note we tested that the containing graph was the dataset's
            // default graph.
            // Easy case.
            OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern());
            return execute(opBGP, input);
        }
        // Not default graph - (graph .... )
        OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern());
        OpGraph op = new OpGraph(quadPattern.getGraphNode(), opBGP);
        return execute(op, input);
    }
}
