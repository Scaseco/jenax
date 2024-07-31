package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public abstract class AggStateTransitionBase<I, E, K, V>
    implements AggStateTransition<I, E, K, V>
{
    protected Object matchStateId;

    public AggStateTransitionBase(Object matchStateId) {
        super();
        this.matchStateId = matchStateId;
    }

    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }
}
