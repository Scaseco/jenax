package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateObject;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTypeProduceNode;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateObject<I, E, K, V>
    extends AggStateMemberSet<I, E, K, V>
    implements AggStateTypeProduceNode<I, E, K, V>
{

    @SafeVarargs
    // public static <I, E, K, V> AggStateObject<I, E, K, V> of(AggStateGon<I, E, K, V> ...edgeAggregators) {
    public static <I, E, K, V> AggStateObject<I, E, K, V> of(AggStateTransition<I, E, K, V> ...edgeAggregators) {
        AggStateObject<I, E, K, V> result = new AggStateObject<>();
        for (AggStateTransition<I, E, K, V> agg : edgeAggregators) {
            result.addPropertyAggregator(agg);
        }
        return result;
    }

    @Override
    public GonType getGonType() {
        return GonType.OBJECT;
    }

    // protected void addPropertyAggregator(AggStateTypeProduceEntry<I, E, K, V> propertyAggregator) {
//    protected void addPropertyAggregator(AggStateTransition<I, E, K, V> propertyAggregator) {
//        // XXX Validate that there is no clash in member keys
//        Object matchStateId = propertyAggregator.getMatchStateId();
//        memberAggs.put(matchStateId, propertyAggregator);
//    }

    @Override
    public AccStateObject<I, E, K, V> newAccumulator() {
        MemberAccs<I, E, K, V> subAccs = buildMemberAccs();
        AccStateObject<I, E, K, V> result = AccStateObject.of(subAccs.fieldIdToIndex(), subAccs.edgeAccs());
        return result;
    }
}
