package org.aksw.jenax.graphql.sparql.v2.agg.state.impl;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceNode;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateLiteral;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateTypeProduceNode;
import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;

public class AggStateLiteral<I, E, K, V>
    implements AggStateTypeProduceNode<I, E, K, V>
{
    protected BiFunction<I, E, ? extends V> inputToValue;

    protected AggStateLiteral(BiFunction<I, E, ? extends V> inputToValue) {
        super();
        this.inputToValue = inputToValue;
    }

    public static <I, E, K, V> AggStateLiteral<I, E, K, V> of(BiFunction<I, E, ? extends V> inputToValue) {
        return new AggStateLiteral<>(inputToValue);
    }

    public static <I, E, K, V> AggStateLiteral<I, E, K, V> of(Class<K> keyClz, BiFunction<I, E, ? extends V> inputToValue) {
        return new AggStateLiteral<>(inputToValue);
    }

    public static <I, E, K, V, X> AggStateLiteral<I, E, K, V> of(BiFunction<I, E, X> inputToTmp, Function<? super X, ? extends V> tmpToValue) {
        BiFunction<I, E, V> composite = inputToTmp.andThen(tmpToValue);
        return of(composite);
    }

    @Override
    public GonType getGonType() {
        return GonType.LITERAL;
    }

    @Override
    public AccStateTypeProduceNode<I, E, K, V> newAccumulator() {
        return AccStateLiteral.of(inputToValue);
    }
}
