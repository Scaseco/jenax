package org.aksw.jena_sparql_api.core.utils;

import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.base.Function;

public class F_TripleToBinding
    implements Function<Triple, Binding>
{
    @Override
    public Binding apply(Triple triple) {
        Binding result = TripleUtils.tripleToBinding(triple);
        return result;
    }

    public static final F_TripleToBinding fn = new F_TripleToBinding();
}
