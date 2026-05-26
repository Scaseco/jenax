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

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

/**
 * Conversion utilities between {@link Binding} (Node-based) and {@link BindingNodeId} (NodeId-based).
 * <p>
 * This class provides the same conversion functionality as {@link SolverLibTDB} but is
 * packaged in jena-leapfrogjoin to avoid exposing package-private methods in jena-tdb2.
 * The implementations are identical, using only public APIs from jena-tdb2.
 */
public class BindingIdConverter {

    private BindingIdConverter() {}

    /**
     * Convert a {@link Binding} ({@code Var→Node}) to a {@link BindingNodeId} ({@code Var→NodeId}).
     * <p>
     * Uses the NodeTable cache for efficiency — repeated lookups of the same Node are resolved once.
     *
     * @param binding the binding to convert
     * @param nodeTable the node table for resolving NodeId lookups
     * @return a BindingNodeId with the same variable bindings
     */
    public static BindingNodeId convert(Binding binding, NodeTable nodeTable) {
        if (binding instanceof BindingTDB bindingTDB) {
            return bindingTDB.getBindingId();
        }

        BindingNodeId b = new BindingNodeId(binding);
        Iterator<Var> vars = binding.vars();

        for (; vars.hasNext(); ) {
            Var v = vars.next();
            Node n = binding.get(v);
            if (n == null)
                continue;

            NodeId id = nodeTable.getNodeIdForNode(n);
            b.put(v, id);
        }
        return b;
    }

    /**
     * Convert a {@link BindingNodeId} ({@code Var→NodeId}) to a {@link Binding} ({@code Var→Node}).
     * <p>
     * Returns a {@link BindingTDB} which lazily resolves NodeIds to Nodes on-demand,
     * avoiding unnecessary NodeTable lookups during intermediate query processing.
     *
     * @param bindingNodeIds the node-ID-based binding to convert
     * @param nodeTable the node table for resolving Node lookups
     * @return a Binding (implemented as BindingTDB) that wraps the binding node IDs
     */
    public static Binding convToBinding(BindingNodeId bindingNodeIds, NodeTable nodeTable) {
        return new BindingTDB(bindingNodeIds, nodeTable);
    }
}
