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

import org.aksw.jenax.arq.util.tuple.resultset.ResultStreamer;
import org.apache.jena.atlas.lib.tuple.Tuple;

/**
 * The essential method for running tuple queries
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <TupleType>
 * @param <ComponentType>
 */
public interface TupleQuerySupport<TupleType, ComponentType> {

     /** Method for running tuple queries */
     ResultStreamer<TupleType, ComponentType, Tuple<ComponentType>> find(TupleQuery<ComponentType> tupleQuery);

     /** Maybe the tuple table should be able to tell if a value is a placeholder? */
     // boolean isAny(ComponentType value);
}
