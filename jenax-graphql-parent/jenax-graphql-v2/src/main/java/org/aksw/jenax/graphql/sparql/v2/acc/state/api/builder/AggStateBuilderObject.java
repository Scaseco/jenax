package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateObject;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateBuilderObject<I, E, K, V>
    extends AggStateBuilderObjectLikeBase<I, E, K, V>
    implements AggStateBuilderNode<I, E, K, V>
{
    @Override
    public GonType getGonType() {
        return GonType.OBJECT;
    }

    @Override
    public String toString() {
        return "AggStateBuilderObject [propertyMappers=" + edgeMappers + "]";
    }

    @Override
    public AggStateObject<I, E, K, V> newAggregator() {
        AggStateTransition<I, E, K, V>[] subAggs = buildSubAggs();
        AggStateObject<I, E, K, V> result = AggStateObject.of(subAggs);
        return result;
    }
}
