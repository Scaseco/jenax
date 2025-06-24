package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateFragmentHead;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateFragmentHead<I, E, K, V>
    implements AggStateTransition<I, E, K, V>
{
    protected Object matchStateId;
    protected AggStateGon<I, E, K, V> fragmentBody;

    public AggStateFragmentHead(Object matchStateId) {
        super();
        this.matchStateId = matchStateId;
    }

    // @SafeVarargs
    // public static <I, E, K, V> AggStateObject<I, E, K, V> of(AggStateGon<I, E, K, V> ...edgeAggregators) {
    public static <I, E, K, V> AggStateFragmentHead<I, E, K, V> of(Object matchStateId, AggStateGon<I, E, K, V> fragmentBody) {
        AggStateFragmentHead<I, E, K, V> result = new AggStateFragmentHead<>(matchStateId);
        result.fragmentBody = fragmentBody;
//        for (AggStateTransition<I, E, K, V> agg : edgeAggregators) {
//            result.addPropertyAggregator(agg);
//        }
        return result;
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }

    @Override
    public GonType getGonType() {
        return GonType.ENTRY;
    }

    @Override
    public AccStateTypeTransition<I, E, K, V> newAccumulator() {
        AccStateGon<I, E, K, V> targetAcc = fragmentBody.newAccumulator();
        AccStateFragmentHead<I, E, K, V> result = AccStateFragmentHead.of(matchStateId, targetAcc);
        return result;
    }


//    public static <I, E, K, V> AggStateObject<I, E, K, V> ofList(Collection<? extends AggStateTypeProduceEntry<I, E, K, V>> edgeAggregators) {
//        AggStateObject<I, E, K, V> result = new AggStateObject<>();
//        for (AggStateTypeProduceEntry<I, E, K, V> agg : edgeAggregators) {
//            result.addPropertyAggregator(agg);
//        }
//        return result;
//    }

//    @Override
//    public Map<Object, AggStateTransition<I, E, K, V>> getPropertyAggregators() {
//        return memberAggs;
//    }

    // protected void addPropertyAggregator(AggStateTypeProduceEntry<I, E, K, V> propertyAggregator) {
//    protected void addPropertyAggregator(AggStateTransition<I, E, K, V> propertyAggregator) {
//        // XXX Validate that there is no clash in member keys
//        Object matchStateId = propertyAggregator.getMatchStateId();
//        memberAggs.put(matchStateId, propertyAggregator);
//    }


//    @Override
//    public AccStateTypeTransition<I, E, K, V> newAccumulator() {
//        int n = memberAggs.size();
//
//        Map<Object, Integer> fieldIdToIndex = new HashMap<>();
//        AccStateTypeTransition<I, E, K, V>[] edgeAccs = new AccStateTypeTransition[n];
//
//        int fieldIndex = 0;
//        for (Entry<Object, AggStateTransition<I, E, K, V>> e : memberAggs.entrySet()) {
//            Object matchStateId = e.getKey();
//            AggStateTransition<I, E, K, V> agg = e.getValue();
//
//            AccStateTypeTransition<I, E, K, V> acc = agg.newAccumulator();
//
//            fieldIdToIndex.put(matchStateId, fieldIndex);
//            edgeAccs[fieldIndex] = acc;
//            ++fieldIndex;
//        }
//
//        AccStateFragment<I, E, K, V> result = AccStateFragment.of(matchStateId, fieldIdToIndex, edgeAccs);
//        return result;
//    }
}
