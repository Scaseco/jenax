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

package org.aksw.jenax.arq.util.tuple.adapter;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge4;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.tuple.QuadAccessor;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class TupleBridgeQuad
    implements TupleBridge4<Quad, Node>
{
    public static final TupleBridgeQuad INSTANCE = new TupleBridgeQuad();

    public static TupleBridgeQuad get() {
        return INSTANCE;
    }

    @Override
    public int getDimension() {
        return 4;
    }

    @Override
    public Node get(Quad quad, int idx) {
        return QuadUtils.getNode(quad, idx);
    }

    @Override
    public Quad build(Node g, Node s, Node p, Node o) {
        return Quad.create(g, s, p, o);
    }

    public static <X, C> C getGraph(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        return accessor.get(tuple, 0);
    }

    public static <X, C> C getSubject(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        return accessor.get(tuple, 1);
    }

    public static <X, C> C getPredicate(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        return accessor.get(tuple, 2);
    }

    public static <X, C> C getObject(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        return accessor.get(tuple, 3);
    }

    // Experiment to have a domain view over a TupleAccessor

    public static <X, C> QuadAccessor<X, C> bind(TupleAccessor<X, C> accessor) {
        return new QuadAccessorImpl<>(accessor);
    }

    public static class QuadAccessorImpl<X, C>
        implements QuadAccessor<X, C>
    {
        protected TupleAccessor<X, C> accessor;

        public QuadAccessorImpl(TupleAccessor<X, C> accessor) {
            super();
            this.accessor = accessor;
        }
        @Override public C getGraph(X tuple) { return TupleBridgeQuad.getGraph(tuple, accessor); }
        @Override public C getSubject(X tuple) { return TupleBridgeQuad.getSubject(tuple, accessor); }
        @Override public C getPredicate(X tuple) { return TupleBridgeQuad.getPredicate(tuple, accessor); }
        @Override public C getObject(X tuple) { return TupleBridgeQuad.getObject(tuple, accessor); }

    }
}
