package org.aksw.jenax.constraint.api;

import java.util.function.Function;

import com.google.common.collect.Range;

/**
 * A value space intensionally describes a set of values (possibly across datatypes).
 * Because of its set nature, it is possible to form intersections and unions and test
 * for emptiness.
 *
 * @author raven
 */
public interface VSpace
    extends Constrainable
{
    @Override
    VSpace clone();

    @Override
    VSpace stateIntersection(VSpace valueSpace);

    @Override
    VSpace stateUnion(VSpace valueSpace);

    /**
     * Create a new closed value space, add the full range of the given dimension and
     * intersect it with 'this'.
     */
    VSpace forDimension(Object dimensionKey);

    /**
     * Copy the values of the dimension fromDimKey to toDimKey.
     * Replaces the target values. There is no consistency check (e.g. moving a numeric range to the iri dimension)
     * Use with care.
     */
    // TODO Think of a better/safer/validating way
    // VSpace moveDimension(Object fromDimKey, Object toDimKey);

    /** Create a new value space by mapping the values from a given dimension
     *  Issue: We don't know if the mapping is surjective - i.e. if the src range is unconstrained, the tgt range in that dimension may yet be constrained.
     *  */
    <X extends Comparable<X>> VSpace mapDimensionToNewVSpace(Object fromDimKey, Class<X> itemType, Function<Range<X>, Range<X>> mapper);

    /** Whether the value space only has ranges in the dimension with the given key (e.g. numeric, iRI, text, ...) */
    boolean isLimitedTo(Object dimensionKey);

    /** Whether the set of values described by this value space is empty.
     * Technically an alias for isConflicting */
    default boolean isEmpty() {
        return isConflicting();
    }
}
