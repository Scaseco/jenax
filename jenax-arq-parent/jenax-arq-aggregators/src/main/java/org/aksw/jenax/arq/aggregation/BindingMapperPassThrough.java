package org.aksw.jenax.arq.aggregation;

import org.apache.jena.sparql.engine.binding.Binding;

public class BindingMapperPassThrough
    implements BindingMapper<Binding>
{
    public Binding apply(Binding binding, Long rowNum) {
        return binding;
    }
}