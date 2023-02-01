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

package org.aksw.jenax.arq.util.tuple.resultset;

import org.apache.jena.atlas.lib.tuple.Tuple;

/**
 * Helper interface for use as a return type and in lambdas
 * for creating a
 * {@link ResultStreamer} instance from typically a store object
 * obtained via {@link StorageNodeMutable#newStore()}
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain tuple type
 * @param <C> The component type
 * @param <T> The tuple type such as {@link Tuple}
 */
public interface ResultStreamerBinder<D, C, T>
{
    ResultStreamer<D, C, T> bind(Object store);
}
