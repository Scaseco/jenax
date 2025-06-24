package org.aksw.jenax.arq.util.binding;

import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;

public class BindingMapperTriple
    implements BindingMapper<Triple>
{
    protected Triple triple;

    public BindingMapperTriple(Triple triple) {
        this.triple = triple;
    }

    @Override
    public Triple apply(Binding binding, Long rowNum) {
        Triple result = TripleUtils.copySubstitute(triple, binding);
        return result;
    }
}
