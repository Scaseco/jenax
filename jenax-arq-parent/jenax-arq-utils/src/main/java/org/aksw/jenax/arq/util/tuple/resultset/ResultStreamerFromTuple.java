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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.tuple.bridge.TupleBridge;
import org.apache.jena.atlas.lib.tuple.Tuple;

/**
 * Implementation backed by a supplier of streams of {@link Tuple}s.
 * Can convert to domain and component representation (if applicable).
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type such as Quad
 * @param <C> The component type such as Node
 */
public class ResultStreamerFromTuple<D, C>
    implements ResultStreamer<D, C, Tuple<C>>
{
    /** The dimension of the tuples returned by the tuple streamer */
    protected int tupleDimension;
    protected Supplier<Stream<Tuple<C>>> tupleStreamer;
    protected TupleBridge<D, C> domainAccessor;

    public ResultStreamerFromTuple(
            int tupleDimension,
            Supplier<Stream<Tuple<C>>> tupleStreamer,
            TupleBridge<D, C> domainAccessor) {
        super();
        this.tupleDimension = tupleDimension;
        this.tupleStreamer = tupleStreamer;
        this.domainAccessor = domainAccessor;
    }

    /**
     * Only works if the accessor can create domain objects with a single component
     */
    @Override
    public Stream<D> streamAsDomainObject() {
        int domainDimension = domainAccessor.getDimension();
        if (domainDimension != tupleDimension) {
            throw new UnsupportedOperationException("Tuple dimension " + tupleDimension + " does not match domain dimension " + domainDimension);
        }

        return streamAsTuple().map(tuple -> domainAccessor.build(tuple, Tuple::get));
    }

    @Override
    public Stream<C> streamAsComponent() {
        if (tupleDimension != 1) {
            throw new UnsupportedOperationException("Cannot stream domain objects with dimension != 1 as a component");
        }

        return streamAsTuple().map(tuple -> tuple.get(0));
    }

    @Override
    public Stream<Tuple<C>> streamAsTuple() {
        return tupleStreamer.get();
    }

    @Override
    public BackingType getBackingType() {
        return BackingType.TUPLE;
    }

}