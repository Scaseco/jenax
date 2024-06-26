package org.aksw.jenax.arq.aggregation;

import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

public class AccComputeBinding<B, E, T>
    implements Accumulator<B, E, T>
{
    //protected org.apache.jena.sparql.expr.aggregate.Accumulator delegate;
    protected Map<Var, Function<B, Node>> varToNodeFn;
    protected Accumulator<Binding, E, T> delegate;

    public AccComputeBinding(Map<Var, Function<B, Node>> varToNodeFn, Accumulator<Binding, E, T> delegate) {
        super();
        this.varToNodeFn = varToNodeFn;
        this.delegate = delegate;
    }

    @Override
    public void accumulate(B input, E env) {
        BindingBuilder builder = BindingBuilder.create();

        varToNodeFn.forEach((key, value) -> {
            Node node = value.apply(input);
            builder.add(key, node);
        });

        delegate.accumulate(builder.build(), env);
    }

    @Override
    public T getValue() {
        T result = delegate.getValue();
        return result;
    }
}
