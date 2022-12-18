package org.aksw.jenax.arq.aggregation;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class AggJenaWrapper
    implements Aggregator<Binding, FunctionEnv, NodeValue>
{
    protected org.apache.jena.sparql.expr.aggregate.Aggregator delegate;

    public AggJenaWrapper(org.apache.jena.sparql.expr.aggregate.Aggregator delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Accumulator<Binding, FunctionEnv, NodeValue> createAccumulator() {
        AccJenaWrapper result = new AccJenaWrapper(delegate.createAccumulator());
        return result;
    }
}
