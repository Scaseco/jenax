package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntry;

public abstract class AccStateTransitionBase<I, E, K, V>
    extends AccStateBase<I, E, K, V>
    implements AccStateTypeProduceEntry<I, E, K, V>
{
    protected Object matchStateId;

    public AccStateTransitionBase(Object matchStateId) {
        super();
        this.matchStateId = matchStateId;
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }
}
