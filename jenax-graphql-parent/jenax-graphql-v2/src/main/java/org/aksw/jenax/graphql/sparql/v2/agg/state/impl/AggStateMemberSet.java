package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;

public abstract class AggStateMemberSet<I, E, K, V>
    implements AggStateGon<I, E, K, V>
{
    protected Map<Object, AggStateTransition<I, E, K, V>> memberAggs = new LinkedHashMap<>();

    public AggStateMemberSet() {
        super();
    }

//    @SafeVarargs
//    // public static <I, E, K, V> AggStateObject<I, E, K, V> of(AggStateGon<I, E, K, V> ...edgeAggregators) {
//    public static <I, E, K, V> AggStateMemberSet<I, E, K, V> of(AggStateTransition<I, E, K, V> ...edgeAggregators) {
//      AggStateFragmentBody<I, E, K, V> result = new AggStateFragmentBody<>();
//      for (AggStateTransition<I, E, K, V> agg : edgeAggregators) {
//          result.addPropertyAggregator(agg);
//      }
//      return result;
//  }

//  @Override
//  public Object getMatchStateId() {
//      return matchStateId;
//  }

//  public static <I, E, K, V> AggStateObject<I, E, K, V> ofList(Collection<? extends AggStateTypeProduceEntry<I, E, K, V>> edgeAggregators) {
//      AggStateObject<I, E, K, V> result = new AggStateObject<>();
//      for (AggStateTypeProduceEntry<I, E, K, V> agg : edgeAggregators) {
//          result.addPropertyAggregator(agg);
//      }
//      return result;
//  }

    public Map<Object, AggStateTransition<I, E, K, V>> getPropertyAggregators() {
        return memberAggs;
    }

    // protected void addPropertyAggregator(AggStateTypeProduceEntry<I, E, K, V> propertyAggregator) {
    protected void addPropertyAggregator(AggStateTransition<I, E, K, V> propertyAggregator) {
        // XXX Validate that there is no clash in member keys
        Object matchStateId = propertyAggregator.getMatchStateId();
        memberAggs.put(matchStateId, propertyAggregator);
    }

    @Override
    public abstract AccStateGon<I, E, K, V> newAccumulator();

    public static record MemberAccs<I, E, K, V>(Map<Object, Integer> fieldIdToIndex, AccStateTypeTransition<I, E, K, V>[] edgeAccs) {}

    public MemberAccs<I, E, K, V> buildMemberAccs() {
        int n = memberAggs.size();

        Map<Object, Integer> fieldIdToIndex = new HashMap<>();
        @SuppressWarnings("unchecked")
        AccStateTypeTransition<I, E, K, V>[] edgeAccs = new AccStateTypeTransition[n];

        int fieldIndex = 0;
        for (Entry<Object, AggStateTransition<I, E, K, V>> e : memberAggs.entrySet()) {
            Object matchStateId = e.getKey();
            AggStateTransition<I, E, K, V> agg = e.getValue();

            AccStateTypeTransition<I, E, K, V> acc = agg.newAccumulator();

            fieldIdToIndex.put(matchStateId, fieldIndex);
            edgeAccs[fieldIndex] = acc;
            ++fieldIndex;
        }

        // AccStateFragmentBody<I, E, K, V> result = AccStateFragmentBody.of(fieldIdToIndex, edgeAccs);
        return new MemberAccs<>(fieldIdToIndex, edgeAccs);
    }
}
