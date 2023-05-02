package org.aksw.jenax.arq.aggregation;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.collector.domain.Aggregator;

/**
 * A bottom up aggregator builder: Starting with a specific aggregator it is expanded upon.
 *
 * @author raven
 *
 * @param <B>
 * @param <T>
 */
public class AggregatorBuilder<B, E, T> {

    protected Aggregator<B, E, T> state;

    public AggregatorBuilder(Aggregator<B, E, T> state) {
        super();
        this.state = state;
    }

    public Aggregator<B, E, T> get() {
        return state;
    }

    public <K> AggregatorBuilder<B, E, Map<K, T>> wrapWithMap(Function<B, K> bindingToKey) {
        Aggregator<B, E, Map<K, T>> agg = AggMap2.create(bindingToKey, state);

        return new AggregatorBuilder<>(agg);
    }


    /**
     * Wrap an inner aggregator from e.g. String -> Set<String>
     * so that given a jena Binding, each (var, node) pair is mapped to a (var, string) pair.
     * the resulting aggregator yields for each binding's var the Set<String> of the inner aggregator
     */
    public <X, K, V> AggregatorBuilder<X, E, Map<K, T>> wrapWithMultiplexDynamic(
            Function<? super X, ? extends Iterator<? extends K>> keyMapper,
            BiFunction<? super X, ? super K, ? extends B> valueMapper) {
        Aggregator<B, E, T> local = state;
        Aggregator<X, E, Map<K, T>> agg = () -> AccMultiplexDynamic.create(keyMapper, valueMapper, local);

        return new AggregatorBuilder<>(agg);
    }

    public <O> AggregatorBuilder<B, E, O> wrapWithTransform(Function<? super T, O> transform) {
        Aggregator<B, E, O> agg = AggTransform2.create(state, transform);

        return new AggregatorBuilder<>(agg);
    }

    public AggregatorBuilder<B, E, T> wrapWithCondition(Predicate<B> predicate) {
        // TODO Is this correct??? i.e. calling createAccumulator here
        Aggregator<B, E, T> local = state;
        Aggregator<B, E, T> agg = () -> AccCondition.create(predicate, local.createAccumulator());

        return new AggregatorBuilder<>(agg);
    }

    public <U> AggregatorBuilder<U, E, T> wrapWithBindingTransform(Function<? super U, B> transform) {
        Aggregator<B, E, T> local = state;
        Aggregator<U, E, T> agg = () -> AccBindingTransform.create(transform, local.createAccumulator());

        return new AggregatorBuilder<>(agg);
    }

//    public static <B, T> AggregatorBuilder<B, T> from(Supplier<Accumulator<B, T>> accSupplier) {
//        Aggregator<B, T> agg = () -> accSupplier.get();
//
//        return new AggregatorBuilder<>(agg);
//    }

    public static <B, E, T> AggregatorBuilder<B, E, T> from(Aggregator<B, E, T> agg) {
        return new AggregatorBuilder<>(agg);
    }

    // combine: BiFunction<I, I> -> T
//    public static <B, T> AggregatorBuilder<B, T> from(Aggregator<B, ? extends T> a, Aggregator<B, ? extends T> b, BiFunction<? super T, ? super T, T> combiner) {
//    	Aggregator<B, T> agg = () -> {
//    		a.createAccumulator();
//    		b.createAccumulator();
//    	}
//    }
}