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

package org.aksw.jenax.arq.util.tuple;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class TupleAccessorQuad
    implements TupleAccessor<Quad, Node>
{
    public static final TupleAccessorQuad INSTANCE = new TupleAccessorQuad();

    @Override
    public int getDimension() {
        return 3;
    }

    @Override
    public Node get(Quad quad, int idx) {
        return QuadUtils.getNode(quad, idx);
    }

    @Override
    public <T> Quad restore(T obj, TupleAccessorCore<? super T, ? extends Node> accessor) {
//        validateRestoreArg(accessor);

        return new Quad(
                accessor.get(obj, 0),
                accessor.get(obj, 1),
                accessor.get(obj, 2),
                accessor.get(obj, 3));
    }

}
