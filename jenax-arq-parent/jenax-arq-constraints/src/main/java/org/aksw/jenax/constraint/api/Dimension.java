package org.aksw.jenax.constraint.api;

import com.google.common.collect.Range;

/** TODO Do we need dimension as an explicit class or is "Domain" sufficient? */
public interface Dimension<T extends Comparable<T>> {
    boolean contains(T value);
    boolean contains(Range<T> range);
}
