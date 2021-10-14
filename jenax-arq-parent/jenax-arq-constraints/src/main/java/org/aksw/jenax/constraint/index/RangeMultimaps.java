package org.aksw.jenax.constraint.index;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

/**
 * Utils to treat a RangeMap<K, Collection<V>> as a MultiRangeMap.
 * Unfortunately guava does not (yet) have such a class.
 */
public class RangeMultimaps {

    public static <K extends Comparable<K>, V, C extends Collection<V>>
    RangeMap<K, C> put(
            RangeMap<K, C> rangeMap,
            Range<K> key, V value,
            Supplier<? extends C> collectionSupplier) {

        RangeMap<K, ? extends Collection<V>> subRangeMap = rangeMap.subRangeMap(key);
        Map<Range<K>, ? extends Collection<V>> subMap = subRangeMap.asMapOfRanges();

        // Append the value to all existing ranges
        for (Collection<V> c : subMap.values()) {
            c.add(value);
        }

        // Then identify the gaps and perform a coalescing put
        RangeSet<K> blocks = ImmutableRangeSet.copyOf(subMap.keySet());
        RangeSet<K> gaps = blocks.complement().subRangeSet(key);

        for (Range<K> gap : gaps.asRanges()) {
            C c = collectionSupplier.get();
            c.add(value);
            rangeMap.putCoalescing(gap, c);
        }

        return rangeMap;
    }
}
