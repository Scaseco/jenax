package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceEntry;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateLiteralProperty;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStatePropertyBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.ArrayMode;

public class AggStateLiteralProperty<I, E, K, V>
    extends AggStatePropertyBase<I, E, K, V>
{
    protected boolean skipIfNull;
    protected BiFunction<I, E, ? extends V> inputToValue;

    protected AggStateLiteralProperty(Object matchStateId, K key, boolean isSingle, boolean skipIfNull, ArrayMode arrayMode, BiFunction<I, E, ? extends V> inputToValue) {
        super(matchStateId, key, isSingle, arrayMode);
        this.skipIfNull = skipIfNull;
        this.inputToValue = inputToValue;
    }

    public static <I, E, K, V> AggStateLiteralProperty<I, E, K, V> of(Object matchStateId, K key, boolean isSingle, boolean skipIfNull, ArrayMode arrayMode, BiFunction<I, E, ? extends V> inputToValue) {
        return new AggStateLiteralProperty<>(matchStateId, key, isSingle, skipIfNull, arrayMode, inputToValue);
    }

    @Override
    public AccStateTypeProduceEntry<I, E, K, V> newAccumulator() {
        return AccStateLiteralProperty.of(matchStateId, memberKey, isSingle, skipIfNull, arrayMode, inputToValue);
    }
}
