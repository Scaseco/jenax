package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateBuilderWrapper<I, E, K, V, X extends AggStateBuilder<I, E, K, V>>
    implements AggStateBuilder<I, E, K, V>
{
    protected X delegate;

    public AggStateBuilderWrapper(X delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public GonType getGonType() {
        return delegate.getGonType();
    }

    @Override
    public AggStateGon<I, E, K, V> newAggregator() {
        return delegate.newAggregator();
    }
}
