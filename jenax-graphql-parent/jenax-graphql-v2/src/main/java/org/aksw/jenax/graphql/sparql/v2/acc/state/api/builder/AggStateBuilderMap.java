package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTypeProduceEntry;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateMap;

public class AggStateBuilderMap<I, E, K, V>
    extends AggStateBuilderEdge<I, E, K, V>
{
    protected BiFunction<I, E, ? extends K> inputToKeyMapper;
    protected BiPredicate<I, E> testIfSingle;

    public AggStateBuilderMap(Object matchStateId, BiFunction<I, E, ? extends K> inputToKeyMapper, BiPredicate<I, E> testIfSingle) {
        super(matchStateId);
        this.inputToKeyMapper = inputToKeyMapper;
        this.testIfSingle = testIfSingle;
    }

    @Override
    public AggStateTypeProduceEntry<I, E, K, V> newAggregator() {
        AggStateGon<I, E, K, V> subAgg = this.targetNodeMapper.newAggregator();
        return AggStateMap.of(matchStateId, inputToKeyMapper, testIfSingle, subAgg);
    }
}
