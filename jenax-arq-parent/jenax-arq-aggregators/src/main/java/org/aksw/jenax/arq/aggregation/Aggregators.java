package org.aksw.jenax.arq.aggregation;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

/**
 * Utils to bridge Aggregators with Java {@link Collector}
 *
 * @author raven
 *
 */
public class Aggregators
{

    /**
     * Create a collector from an aggregator.
     * Combinations of accumulators happens in-place in the larger accumulator.
     *
     * @param <T> The item type used both in the reduction and the output collection
     * @param <C> The output collection type
     * @param aggregator The backing aggregator
     * @return
     */
    public static <T, C extends Collection<T>> Collector<T, Accumulator<T, C>, C> createCollector(
            Aggregator<T, C> aggregator) {

        Collector<T, Accumulator<T, C>, C> result = Collector.of(
                aggregator::createAccumulator,
                Accumulator::accumulate,
                (needle, haystack) -> combineAccumulators(needle, haystack, x -> x, x -> x),
                Accumulator::getValue);

        return result;
    }

    /**
     * {@link #createCollector(Aggregator)} but returning the raw accumulator rather than
     * finishing it to its value
     */
    public static <T, C extends Collection<T>> Collector<T, Accumulator<T, C>, Accumulator<T, C>>
        createCollectorRaw(
            Aggregator<T, C> aggregator,
            UnaryOperator<Accumulator<T, C>> accumulatorCloner) {

        Collector<T, Accumulator<T, C>, Accumulator<T, C>> result = Collector.of(
                aggregator::createAccumulator,
                Accumulator::accumulate,
                (needle, haystack) -> combineAccumulators(needle, haystack, accumulatorCloner, x -> x));

        return result;
    }


    public static <T, V, C extends Collection<V>> Collector<T, Accumulator<T, C>, Accumulator<T, C>>
        createCollectorRaw(
            Aggregator<T, C> aggregator,
            Function<? super V, ? extends T> valueToItem,
            UnaryOperator<Accumulator<T, C>> accumulatorCloner) {

        Collector<T, Accumulator<T, C>, Accumulator<T, C>> result = Collector.of(
                aggregator::createAccumulator,
                Accumulator::accumulate,
                (needle, haystack) -> combineAccumulators(needle, haystack, accumulatorCloner, valueToItem));

        return result;
    }

    /**
     * Merge two accumulators.
     *
     * @param <T>
     * @param <C>
     * @param needle
     * @param haystack
     * @param accumulatorCloner The cloner; may return its argument for in place changes.
     * @return
     */
    public static <T, V, C extends Collection<V>> Accumulator<T, C> combineAccumulators(
            Accumulator<T, C> needle,
            Accumulator<T, C> haystack,
            UnaryOperator<Accumulator<T, C>> accumulatorCloner,
            Function<? super V, ? extends T> valueToItem) {
        if (needle.getValue().size() > haystack.getValue().size()) {
            // Swap
            Accumulator<T, C> tmp = needle; needle = haystack; haystack = tmp;
        }

        Accumulator<T, C> result = accumulatorCloner.apply(haystack);
        for (V value : needle.getValue()) {
            T reductionItem = valueToItem.apply(value);
            result.accumulate(reductionItem);
        }

        return result;
    }
}