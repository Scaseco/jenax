package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTypeProduceEntry;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.ArrayMode;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateProperty;

public class AggStateBuilderProperty<I, E, K, V>
    extends AggStateBuilderEdge<I, E, K, V>
{
    protected boolean isSingle = false; // Only accept a single value (the first one encountered)
    protected K key;
    protected ArrayMode arrayMode;

    public AggStateBuilderProperty(Object matchStateId, ArrayMode arrayMode) {
        super(matchStateId);
        this.arrayMode = arrayMode;
    }

    public static <I, E, K, V> AggStateBuilderProperty<I, E, K, V> of(Object matchStateId, K key, ArrayMode arrayMode) {
        AggStateBuilderProperty<I, E, K, V> result = new AggStateBuilderProperty<>(matchStateId, arrayMode);
        result.setKey(key);
        return result;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        this.isSingle = single;
    }

    @Override
    public AggStateTypeProduceEntry<I, E, K, V> newAggregator() {
        AggStateGon<I, E, K, V> targetAgg = targetNodeMapper.newAggregator();
        AggStateProperty<I, E, K, V> result = AggStateProperty.of(matchStateId, key, isSingle, arrayMode, targetAgg);
        return result;
    }
}
