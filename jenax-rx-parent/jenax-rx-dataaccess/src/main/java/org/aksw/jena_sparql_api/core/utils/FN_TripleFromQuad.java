package org.aksw.jena_sparql_api.core.utils;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import com.google.common.base.Function;

public class FN_TripleFromQuad
    implements Function<Quad, Triple>
{
    public FN_TripleFromQuad() {
        super();
    }

    @Override
    public Triple apply(Quad quad) {
        Triple result = quad.asTriple();
        return result;
    }

    public static final FN_TripleFromQuad fn = new FN_TripleFromQuad();
}
