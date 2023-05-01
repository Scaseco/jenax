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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <ComponentType>
 */
public class TupleQueryImpl<ComponentType>
    implements TupleQuery<ComponentType>
{
    protected int dimension;
    protected List<ComponentType> pattern;
    protected boolean distinct = false;
    protected int[] projection = null;

//    public static <C> TupleQuery<C> create(int dimension) {
//        return new TupleQueryImpl<>(dimension);
//    }

    public static <T> List<T> listOfNulls(int size) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < size; ++i) { result.add(null); }
        return result;
    }

    public TupleQueryImpl(int dimension) {
        super();
        this.dimension = dimension;
        this.pattern = listOfNulls(dimension);
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public TupleQuery<ComponentType> setDistinct(boolean onOrOff) {
        this.distinct = onOrOff;
        return this;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public TupleQuery<ComponentType> setConstraint(int idx, ComponentType value) {
        pattern.set(idx, value);
        return this;
    }

    @Override
    public ComponentType getConstraint(int idx) {
        return pattern.get(idx);
    }

    @Override
    public int[] getProject() {
        return projection;
    }

    @Override
    public TupleQuery<ComponentType> clearProject() {
        projection = null;
        return this;
    }

    @Override
    public List<ComponentType> getPattern() {
        return pattern;
    }

    @Override
    public TupleQuery<ComponentType> setProject(int... tupleIdxs) {
        projection = tupleIdxs;
        return this;
    }

    @Override
    public boolean hasProject() {
        return projection != null;
    }

    @Override
    public Set<Integer> getConstrainedComponents() {
        Set<Integer> result = new LinkedHashSet<Integer>();
        for (int i = 0; i < dimension; ++i) {
            ComponentType value = getConstraint(i);

            // FIXME Check for 'ANY'
            if (value != null) {
                result.add(i);
            }
        }
        return result;
    }


    @Override
    public String toString() {
        String result
            = "SELECT "
            + (isDistinct() ? "DISTINCT " : "")
            + (projection == null
                ? "*"
                : IntStream.of(projection)
                    .mapToObj(Integer::toString).collect(Collectors.joining(" ")))
            + " WHERE "
            + (getConstrainedComponents().isEmpty() ? "TRUE" :
                getConstrainedComponents().stream().map(idx -> "" + idx + "=" + getConstraint(idx))
                .collect(Collectors.joining(" AND ")));

        return result;
    }

    @Override
    public TupleQuery<ComponentType> addProject(int... tupleIdxs) {
        projection = ArrayUtils.addAll(projection, tupleIdxs);
        return this;
    }
}
