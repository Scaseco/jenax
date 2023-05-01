/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.aksw.jenax.arq.util.tuple.query;

import java.util.List;
import java.util.Set;

/**
 * A tuple query comprises projection, distinct and constraints on the level of tuple components
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <ComponentType>
 */
public interface TupleQuery<ComponentType> {
    /** The dimension (number of columns) of the conceptual tuple table this query is intended for */
    int getDimension();

    TupleQuery<ComponentType> setDistinct(boolean onOrOff);
    boolean isDistinct();

    TupleQuery<ComponentType> setConstraint(int idx, ComponentType value);
    ComponentType getConstraint(int idx);

    Set<Integer> getConstrainedComponents();

    List<ComponentType> getPattern();

    /**
     * Baseline tuple query execution on a tuple table.
     * Invokes find(...) on the tupleTable and only afterwards
     * applies filtering, projection and distinct on the obtained stream
     *
     *
     * @return A mutable array for configuration of the projection
     */
    int[] getProject();
    TupleQuery<ComponentType> clearProject();

    boolean hasProject();
    TupleQuery<ComponentType> addProject(int... tupleIdxs);


    /**
     * Replaces a projection with the given one
     *
     * @param tupleIdxs
     * @return
     */
    TupleQuery<ComponentType> setProject(int... tupleIdxs);
}
