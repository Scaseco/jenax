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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.jenax.arq.util.tuple.TupleOps;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.core.Quad;


/**
 * Implementation backed by a supplier of streams of domain objects such as {@link Quad}s.
 * Can convert to tuple and component representation (if applicable).
 * Domain objects can only be converted to component representation if they are logical 1-tuples.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type such as Quad
 * @param <C> The component type such as Node
 */
public class ResultStreamerFromDomain<D, C>
    implements ResultStreamer<D, C, Tuple<C>>
{
    protected Supplier<Stream<D>> domainStreamer;
    protected TupleBridge<D, C> domainAccessor;

    public ResultStreamerFromDomain(Supplier<Stream<D>> domainStreamer, TupleBridge<D, C> domainAccessor) {
        super();
        this.domainStreamer = domainStreamer;
        this.domainAccessor = domainAccessor;
    }

    @Override
    public Stream<D> streamAsDomainObject() {
        return domainStreamer.get();
    }

    @Override
    public Stream<C> streamAsComponent() {
        if (domainAccessor.getDimension() != 1) {
            throw new UnsupportedOperationException("Cannot stream domain objects with dimension != 1 as a component");
        }

        return streamAsDomainObject().map(item -> domainAccessor.get(item,0));
    }

    @Override
    public Stream<Tuple<C>> streamAsTuple() {
        Function<D, Tuple<C>> tupelizer = TupleOps.tupelizer(domainAccessor);
        return streamAsDomainObject().map(tupelizer);
    }

    @Override
    public BackingType getBackingType() {
        return BackingType.DOMAIN;
    }

}
