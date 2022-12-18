package org.aksw.jenax.arq.aggregation;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

/**
 * Aggregator that maps each input item to a single key and allocates
 * a sub aggregator for that key if none exists yet.
 *
 * @author raven
 *
 * @param <B>
 * @param <K>
 * @param <V>
 * @param <C>
 */
public class AggMap2<B, E, K, V, C extends Aggregator<B, E, V>>
    implements Aggregator<B, E, Map<K,V>>
{
    private BiFunction<B, Long, K> mapper;
    private C subAgg;

    public AggMap2(BiFunction<B, Long, K> mapper, C subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public Accumulator<B, E, Map<K, V>> createAccumulator() {
        Accumulator<B, E, Map<K, V>> result = new AccMap2<B, E, K, V, C>(mapper, subAgg);
        return result;
    }

    public static <B, E, K, V, C extends Aggregator<B, E, V>> AggMap2<B, E, K, V, C> create(Function<B, K> mapper, C subAgg) {
        BiFunction<B, Long, K> fn = (binding, rowNum) -> mapper.apply(binding);
        AggMap2<B, E, K, V, C> result = create(fn, subAgg);
        return result;
    }

    public static <B, E, K, V, C extends Aggregator<B, E, V>> AggMap2<B, E, K, V, C> create(BiFunction<B, Long, K> mapper, C subAgg) {
        AggMap2<B, E, K, V, C> result = new AggMap2<B, E, K, V, C>(mapper, subAgg);
        return result;
    }
}
