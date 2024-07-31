package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public abstract class AggStateBuilderEdge<I, E, K, V>
    extends AggStateBuilderTransitionBase<I, E, K, V>
{
    /**
     * Only applicable if the value produced by this PropertyMapper is a json object.
     * If hidden is true, then the owning NodeMapper should merge the produced json object into
     * its own json object.
     */
    protected boolean isHidden = false;

    public AggStateBuilderEdge(Object matchStateId) {
        super(matchStateId);
    }

    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    // public abstract AggStateBuilderNode<I, E, K, V> getTargetNodeMapper();

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }
}
