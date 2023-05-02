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
    public static <T, E, C extends Collection<T>> Collector<T, Accumulator<T, E, C>, C> createCollector(
            Aggregator<T, E, C> aggregator) {

        Collector<T, Accumulator<T, E, C>, C> result = Collector.of(
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
    public static <T, E, C extends Collection<T>> Collector<T, Accumulator<T, E, C>, Accumulator<T, E, C>>
        createCollectorRaw(
            Aggregator<T, E, C> aggregator,
            UnaryOperator<Accumulator<T, E, C>> accumulatorCloner) {

        Collector<T, Accumulator<T, E, C>, Accumulator<T, E, C>> result = Collector.of(
                aggregator::createAccumulator,
                Accumulator::accumulate,
                (needle, haystack) -> combineAccumulators(needle, haystack, accumulatorCloner, x -> x));

        return result;
    }


    public static <T, E, V, C extends Collection<V>> Collector<T, Accumulator<T, E, C>, Accumulator<T, E, C>>
        createCollectorRaw(
            Aggregator<T, E, C> aggregator,
            Function<? super V, ? extends T> valueToItem,
            UnaryOperator<Accumulator<T, E, C>> accumulatorCloner) {

        Collector<T, Accumulator<T, E, C>, Accumulator<T, E, C>> result = Collector.of(
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
    public static <T, E, V, C extends Collection<V>> Accumulator<T, E, C> combineAccumulators(
            Accumulator<T, E, C> needle,
            Accumulator<T, E, C> haystack,
            UnaryOperator<Accumulator<T, E, C>> accumulatorCloner,
            Function<? super V, ? extends T> valueToItem) {
        if (needle.getValue().size() > haystack.getValue().size()) {
            // Swap
            Accumulator<T, E, C> tmp = needle; needle = haystack; haystack = tmp;
        }

        Accumulator<T, E, C> result = accumulatorCloner.apply(haystack);
        for (V value : needle.getValue()) {
            T reductionItem = valueToItem.apply(value);
            result.accumulate(reductionItem);
        }

        return result;
    }
}