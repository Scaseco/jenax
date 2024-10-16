package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateFragmentBody;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;


/**
 * If the state matches, then a set of fields may be emitted.
 * Does not emit an enclosing object.
 */
public class AggStateBuilderFragmentBody<I, E, K, V>
    extends AggStateBuilderObjectLikeBase<I, E, K, V>
{
    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    public AggStateBuilderFragmentBody() {
        super();
    }

    @Override
    public AggStateFragmentBody<I, E, K, V> newAggregator() {
        AggStateTransition<I, E, K, V>[] subAggs = buildSubAggs();
        AggStateFragmentBody<I, E, K, V> result = AggStateFragmentBody.of(subAggs);
        return result;
    }
}
