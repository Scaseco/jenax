package org.aksw.jenax.arq.aggregation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;


/**
 *
 *
 * @author raven
 *
 * @param <B>
 * @param <K>
 * @param <V>
 * @param <C>
 */
public class AccMap2<B, E, K, V, C extends Aggregator<B, E, V>>
    implements Accumulator<B, E, Map<K, V>>
{
    protected BiFunction<B, Long, K> mapper;
    protected C subAgg;

    protected Map<K, Accumulator<B, E, V>> state = new HashMap<>();

    public AccMap2(Function<B, K> mapper, C subAgg) {
        this((binding, rowNum) -> mapper.apply(binding), subAgg);
    }

    public AccMap2(BiFunction<B, Long, K> mapper, C subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public void accumulate(B binding, E env) {
        // TODO Keep track of the relative binding index
        K k = mapper.apply(binding, -1l);
        Accumulator<B, E, V> subAcc = state.get(k);
        if(subAcc == null) {
            subAcc = subAgg.createAccumulator();
            state.put(k, subAcc);
        }
        subAcc.accumulate(binding, env);
    }

    @Override
    public Map<K, V> getValue() {
        Map<K, V> result = new HashMap<K, V>();

        for(Entry<K, Accumulator<B, E, V>> entry : state.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue().getValue();

            result.put(k, v);
        }

        return result;
    }

    public static <B, E, K, V, C extends Aggregator<B, E, V>> AccMap2<B, E, K, V, C> create(Function<B, K> mapper, C subAgg) {
        BiFunction<B, Long, K> fn = (binding, rowNum) -> mapper.apply(binding);
        AccMap2<B, E, K, V, C> result = new AccMap2<>(fn, subAgg);
        return result;
    }

    public static <B, E, K, V, C extends Aggregator<B, E, V>> AccMap2<B, E, K, V, C> create(BiFunction<B, Long, K> mapper, C subAgg) {
        AccMap2<B, E, K, V, C> result = new AccMap2<>(mapper, subAgg);
        return result;
    }

}
