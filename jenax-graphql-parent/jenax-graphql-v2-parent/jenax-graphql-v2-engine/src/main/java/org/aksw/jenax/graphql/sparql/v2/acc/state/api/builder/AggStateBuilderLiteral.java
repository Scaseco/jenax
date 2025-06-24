package org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder;

import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateLiteral;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateBuilderLiteral<I, E, K, V>
    implements AggStateBuilder<I, E, K, V>
{
    // private static final AggStateBuilderLiteral INSTANCE = new AggStateBuilderLiteral();
    // public static <I, E, K, V> AggStateBuilderLiteral<I, E, K, V> get() {
    //    return (AggStateBuilderLiteral<I, E, K, V>)INSTANCE;
    // }

    protected BiFunction<I, E, ? extends V> inputToValue;

    public static <I, E, K, V> AggStateBuilderLiteral<I, E, K, V> of(BiFunction<I, E, ? extends V> inputToValue) {
        AggStateBuilderLiteral<I, E, K, V> result = new AggStateBuilderLiteral<>();
        result.setInputToValue(inputToValue);
        return result;
    }

    public AggStateBuilderLiteral<I, E, K, V> setInputToValue(BiFunction<I, E, ? extends V> inputToValue) {
        this.inputToValue = inputToValue;
        return this;
    }

    public BiFunction<I, E, ? extends V> getInputToValue() {
        return inputToValue;
    }

    protected AggStateBuilderLiteral() {
        super();
    }

    @Override
    public GonType getGonType() {
        return GonType.LITERAL;
    }

    @Override
    public String toString() {
        return "NodeMapperLiteral []";
    }

    @Override
    public AggStateLiteral<I, E, K, V> newAggregator() {
        return AggStateLiteral.of(inputToValue);
    }
}
