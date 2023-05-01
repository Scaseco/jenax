package org.aksw.jenax.constraint.api;

import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.Range;

/**
 * A set of dimensions with an ordering.
 * A domain spans a space for values.
 *
 * @param D The domain key object
 * @param T The value object
 */
public interface Domain<D, T extends Comparable<T>> {
    Set<D> getDimensions();
    Comparator<D> getDimensionComparator();

    default Comparator<Object> getDimensionComparatorRaw() {
        return (x, y) -> getDimensionComparator().compare((D)x, (D)y);
    }

    // classify(T value);
    D classify(Range<T> range);

    VSpace newOpenSpace();
    VSpace newClosedSpace();
}
