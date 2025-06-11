package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

public abstract class AccStatePropertyBase<I, E, K, V>
    extends AccStateTransitionBase<I, E, K, V>
{
    /** The member key (=name) being aggregated */
    protected K memberKey;
    protected boolean isSingle = false;

    protected AccStatePropertyBase(Object matchStateId, K memberKey, boolean isSingle) {
        super(matchStateId);
        this.matchStateId = matchStateId;
        this.memberKey = memberKey;
        this.isSingle = isSingle;
    }

    public K getMemberKey() {
        return memberKey;
    }

    public boolean isSingle() {
        return isSingle;
    }
}
