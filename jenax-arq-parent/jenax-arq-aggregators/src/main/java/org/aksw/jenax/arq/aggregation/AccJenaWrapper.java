package org.aksw.jenax.arq.aggregation;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

public class AccJenaWrapper
    implements Acc<NodeValue>
{
    protected org.apache.jena.sparql.expr.aggregate.Accumulator delegate;

    public AccJenaWrapper(Accumulator delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv env) {
        delegate.accumulate(binding, env);
    }

    @Override
    public NodeValue getValue() {
        return delegate.getValue();
    }
}
