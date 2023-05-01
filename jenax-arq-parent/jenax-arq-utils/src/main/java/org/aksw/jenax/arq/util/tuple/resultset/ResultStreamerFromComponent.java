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
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;

/**
 * Implementation backed by a supplier of streams of components such as {@link Node}s.
 * Can convert to domain and tuple representation (if applicable).
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type such as Quad
 * @param <C> The component type such as Node
 */
public class ResultStreamerFromComponent<D, C>
    implements ResultStreamer<D, C, Tuple<C>>
{
    protected Supplier<Stream<C>> componentStreamer;
    protected TupleBridge<D, C> domainAccessor;

    public ResultStreamerFromComponent(Supplier<Stream<C>> componentStreamer, TupleBridge<D, C> domainAccessor) {
        super();
        this.componentStreamer = componentStreamer;
        this.domainAccessor = domainAccessor;
    }

    /**
     * Only works if the accessor can create domain objects with a single component
     */
    @Override
    public Stream<D> streamAsDomainObject() {
        int domainDimension = domainAccessor.getDimension();
        if (domainDimension != 1) {
            throw new UnsupportedOperationException("Cannot convert component into a domain object of dimension != 1; has" + domainDimension);
        }

        return streamAsComponent().map(
                // We checked that the dimension is 1, so we do not have to check that idx == 0
                component -> domainAccessor.build(component, (x, idx) -> x));
    }

    @Override
    public Stream<C> streamAsComponent() {
        return componentStreamer.get();
    }

    @Override
    public Stream<Tuple<C>> streamAsTuple() {
        return streamAsComponent().map(TupleFactory::create1);
    }

    @Override
    public BackingType getBackingType() {
        return BackingType.COMPONENT;
    }

}
