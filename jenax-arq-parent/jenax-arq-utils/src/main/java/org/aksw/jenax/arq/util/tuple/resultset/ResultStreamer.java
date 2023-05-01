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

import java.util.stream.Stream;

/**
 * A class that acts as a factory for producing streams for the different aspects of tuples.
 * Specializations should convert between domain, tuple and component view whenever applicable.
 *
 * Examples: A component can be converted to a 1-tuple and vice versa.
 * A quad can be converted to a four tuple and vice versa.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type such as Quad
 * @param <C> The component type such as Node
 * @param <T> The tuple type such as Tuple
 */
public interface ResultStreamer<D, C, T> {

    /**
     * Enum to describe whether an instance is backed by domain tuples, components or generic tuples.
     * Can be used to avoid needless transformations
     *
     * Domain implies tuple and tuple implies component. This is transitive, i.e. domain implies component.
     * Items with lower ordinal() imply capabilities for all items with higher one
     */
    public enum BackingType {
        DOMAIN,
        TUPLE,
        COMPONENT
    }

    BackingType getBackingType();


    Stream<D> streamAsDomainObject();
    Stream<C> streamAsComponent();
    Stream<T> streamAsTuple();



    /**
     * A set describing which methods are valid to invoke
     *
     *
     * @return
     */
//    EnumSet<Capabilities> getCapabilities();
//    getCapabilitiy();
}
