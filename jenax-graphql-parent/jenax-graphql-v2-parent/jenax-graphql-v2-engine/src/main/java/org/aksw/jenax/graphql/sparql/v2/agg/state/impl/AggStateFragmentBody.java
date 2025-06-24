package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateFragmentBody;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateFragmentBody<I, E, K, V>
    extends AggStateMemberSet<I, E, K, V>
{
    public AggStateFragmentBody() {
        super();
    }

    @SafeVarargs
    // public static <I, E, K, V> AggStateObject<I, E, K, V> of(AggStateGon<I, E, K, V> ...edgeAggregators) {
    public static <I, E, K, V> AggStateFragmentBody<I, E, K, V> of(AggStateTransition<I, E, K, V> ...edgeAggregators) {
        AggStateFragmentBody<I, E, K, V> result = new AggStateFragmentBody<>();
        for (AggStateTransition<I, E, K, V> agg : edgeAggregators) {
            result.addPropertyAggregator(agg);
        }
        return result;
    }

    @Override
    public GonType getGonType() {
        return GonType.OBJECT;
    }

//    @Override
//    protected void addPropertyAggregator(AggStateTransition<I, E, K, V> propertyAggregator) {
//        // XXX Validate that there is no clash in member keys
//        Object matchStateId = propertyAggregator.getMatchStateId();
//        memberAggs.put(matchStateId, propertyAggregator);
//    }

    @Override
    public AccStateFragmentBody<I, E, K, V> newAccumulator() {
        MemberAccs<I, E, K, V> subAccs = buildMemberAccs();
        AccStateFragmentBody<I, E, K, V> result = AccStateFragmentBody.of(subAccs.fieldIdToIndex(), subAccs.edgeAccs());
        return result;
    }
}
