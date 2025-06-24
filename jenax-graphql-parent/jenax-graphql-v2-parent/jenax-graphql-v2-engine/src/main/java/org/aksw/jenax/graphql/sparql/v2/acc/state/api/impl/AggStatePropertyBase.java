package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

public abstract class AggStatePropertyBase<I, E, K, V>
    extends AggStateTransitionBase<I, E, K, V>
    implements AggStateTypeProduceEntry<I, E, K, V>
{
    /** The member key being aggregated */
    protected K memberKey;
    protected boolean isSingle = false;
    protected ArrayMode arrayMode;

    protected AggStatePropertyBase(Object matchStateId, K memberKey, boolean isSingle, ArrayMode arrayMode) {
        super(matchStateId);
        this.matchStateId = matchStateId;
        this.memberKey = memberKey;
        this.isSingle = isSingle;
        this.arrayMode = arrayMode;
    }

    public K getMemberKey() {
        return memberKey;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public ArrayMode getArrayMode() {
        return arrayMode;
    }
}
