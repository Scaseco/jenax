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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;

/**
 * Partition a BasicPattern into connected components based on variable sharing.
 * <p>
 * Two triples are in the same component if they share at least one variable,
 * either directly or transitively. This is useful for:
 * <ul>
 *   <li>Identifying which patterns can be joined via leap frog</li>
 *   <li>Ordering component execution for efficient query processing</li>
 *   <li>Determining if a pattern has disconnected subgraphs</li>
 * </ul>
 */
public class ComponentPartitioner {
    
    /**
     * Partition a BasicPattern into connected components.
     * <p>
     * A connected component is a set of triples where every triple is connected
     * to every other triple via shared variables (directly or through a chain).
     * 
     * @param pattern The basic pattern to partition
     * @return List of components, where each component is a list of triples
     */
    public static List<List<Triple>> partition(BasicPattern pattern) {
        if (pattern.size() <= 1) {
            List<List<Triple>> result = new ArrayList<>();
            if (pattern.size() == 1) {
                result.add(pattern.getList());
            }
            return result;
        }
        
        int n = pattern.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        
        // Union-Find with path compression
        java.util.function.IntUnaryOperator find = new java.util.function.IntUnaryOperator() {
            @Override
            public int applyAsInt(int i) {
                if (parent[i] != i) {
                    parent[i] = this.applyAsInt(parent[i]);
                }
                return parent[i];
            }
        };
        
        // Union two components
        java.util.function.IntBinaryOperator union = new java.util.function.IntBinaryOperator() {
            @Override
            public int applyAsInt(int i, int j) {
                int rootI = find.applyAsInt(i);
                int rootJ = find.applyAsInt(j);
                if (rootI != rootJ) {
                    parent[rootI] = rootJ;
                }
                return rootJ;
            }
        };
        
        // Build variable -> triples mapping
        Set<Var>[] tripleVars = new Set[n];
        for (int i = 0; i < n; i++) {
            tripleVars[i] = getVariables(pattern.get(i));
        }
        
        // Union triples that share variables
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (shareVariable(tripleVars[i], tripleVars[j])) {
                    union.applyAsInt(i, j);
                }
            }
        }
        
        // Group by root using a map to ensure correct assignment
        Map<Integer, List<Triple>> rootToComponent = new HashMap<>();
        
        for (int i = 0; i < n; i++) {
            int root = find.applyAsInt(i);
            rootToComponent.computeIfAbsent(root, k -> new ArrayList<>()).add(pattern.get(i));
        }
        
        return new ArrayList<>(rootToComponent.values());
    }
    
    /**
     * Get all variables mentioned in a triple.
     */
    private static Set<Var> getVariables(Triple triple) {
        Set<Var> vars = new HashSet<>();
        if (triple.getSubject().isVariable()) {
            vars.add(Var.alloc(triple.getSubject()));
        }
        if (triple.getPredicate().isVariable()) {
            vars.add(Var.alloc(triple.getPredicate()));
        }
        if (triple.getObject().isVariable()) {
            vars.add(Var.alloc(triple.getObject()));
        }
        return vars;
    }
    
    /**
     * Check if two sets of variables have any intersection.
     */
    private static boolean shareVariable(Set<Var> vars1, Set<Var> vars2) {
        for (Var v : vars1) {
            if (vars2.contains(v)) {
                return true;
            }
        }
        return false;
    }
}
