package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntry;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateMap;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTypeProduceEntry;

public class AggStateMap<I, E, K, V>
    implements AggStateTypeProduceEntry<I, E, K, V>
{
    protected Object matchStateId;
    protected BiFunction<I, E, ? extends K> inputToKeyMapper;
    protected BiPredicate<I, E> testIfSingle;
    protected AggStateGon<I, E, K, V> subAgg;

    public AggStateMap(Object matchStateId, BiFunction<I, E, ? extends K> inputToKeyMapper, BiPredicate<I, E> testIfSingle, AggStateGon<I, E, K, V> subAgg) {
        super();
        this.matchStateId = matchStateId;
        this.inputToKeyMapper = inputToKeyMapper;
        this.testIfSingle = testIfSingle;
        this.subAgg = subAgg;
    }

    // sub agg must be of category produce node - AggStateTypeProduceNode
    public static <I, E, K, V> AggStateMap<I, E, K, V> of(Object matchStateId, BiFunction<I, E, ? extends K> inputToKeyMapper, BiPredicate<I, E> testIfSingle, AggStateGon<I, E, K, V> subAgg) {
        return new AggStateMap<>(matchStateId, inputToKeyMapper, testIfSingle, subAgg);
    }

    @Override
    public AccStateTypeProduceEntry<I, E, K, V> newAccumulator() {
        AccStateGon<I, E, K, V> subAcc = subAgg.newAccumulator();
        AccStateMap<I, E, K, V> result = new AccStateMap<>(matchStateId, inputToKeyMapper, testIfSingle, subAcc);
        subAcc.setParent(result);
        return result;
    }

    @Override
    public Object getMatchStateId() {
        return matchStateId;
    }
}
