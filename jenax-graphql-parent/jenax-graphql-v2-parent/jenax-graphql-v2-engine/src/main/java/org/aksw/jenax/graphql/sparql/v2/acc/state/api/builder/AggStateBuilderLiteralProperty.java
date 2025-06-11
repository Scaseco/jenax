package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.ArrayMode;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateLiteralProperty;

/** Literal properties can suppress emitting null values. */
public class AggStateBuilderLiteralProperty<I, E, K, V>
    extends AggStateBuilderTransitionMatch<I, E, K, V>
{
    protected BiFunction<I, E, ? extends V> inputToValue;
    protected boolean skipIfNull;
    protected boolean isSingle = false; // Only accept a single value (the first one encountered)
    protected ArrayMode arrayMode;
    protected K key;

    public AggStateBuilderLiteralProperty(Object matchStateId) {
        super(matchStateId);
    }

    public static <I, E, K, V> AggStateBuilderLiteralProperty<I, E, K, V> of(Object matchStateId, K key, boolean isSingle, boolean skipIfNull, ArrayMode arrayMode, BiFunction<I, E, ? extends V> inputToValue) {
        AggStateBuilderLiteralProperty<I, E, K, V> result = new AggStateBuilderLiteralProperty<>(matchStateId);
        result.key = key;
        result.isSingle = isSingle;
        result.skipIfNull = skipIfNull;
        result.inputToValue = inputToValue;
        result.arrayMode = arrayMode;
        return result;
    }

    @Override
    public AggStateTransition<I, E, K, V> newAggregator() {
        return AggStateLiteralProperty.of(matchStateId, key, isSingle, skipIfNull, arrayMode, inputToValue);
    }
}
