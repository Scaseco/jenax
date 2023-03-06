package org.aksw.jenax.constraint.api;

import com.google.common.collect.Range;

public interface Dimension {
    boolean contains(Object value);
    boolean contains(Range<?> range);
}
