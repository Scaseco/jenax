package org.aksw.jenax.arq.aggregation;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

public class BindingMapperQuad
    implements BindingMapper<Quad>
{
    protected Quad quad;

    public BindingMapperQuad(Quad quad) {
        this.quad = quad;
    }

    @Override
    public Quad apply(Binding binding, Long rowNum) {
        Quad result = QuadUtils.copySubstitute(quad, binding);
        return result;
    }
}
