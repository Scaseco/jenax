package org.aksw.jenax.arq.aggregation;

import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

public class AccComputeBinding<B, T>
    implements Accumulator<B, T>
{
    //protected org.apache.jena.sparql.expr.aggregate.Accumulator delegate;
    protected Map<Var, Function<B, Node>> varToNodeFn;
    protected Accumulator<Binding, T> delegate;

    public AccComputeBinding(Map<Var, Function<B, Node>> varToNodeFn, Accumulator<Binding, T> delegate) {
        super();
        this.varToNodeFn = varToNodeFn;
        this.delegate = delegate;
    }

    @Override
    public void accumulate(B input) {
        BindingBuilder builder = BindingBuilder.create();

        varToNodeFn.forEach((key, value) -> {
            Node node = value.apply(input);
            builder.add(key, node);
        });

        delegate.accumulate(builder.build());
    }

    @Override
    public T getValue() {
        T result = delegate.getValue();
        return result;
    }

}
