package org.aksw.jenax.constraint.api;

/**
 * A value space intensionally describes a set of values (possibly across datatypes).
 * Because of its set nature, it is possible to form intersections and unions and test
 * for emptiness.
 *
 * @author raven
 *
 */
public interface ValueSpace
    extends Constrainable
{
    @Override
    ValueSpace clone();

    @Override
    ValueSpace stateIntersection(ValueSpace valueSpace);

    @Override
    ValueSpace stateUnion(ValueSpace valueSpace);

    /**
     * Create a new closed value space, add the full range of the given dimension and
     * intersect it with 'this'.
     */
    ValueSpace forDimension(Object dimensionKey);

    /**
     * Copy the values of the dimension fromDimKey to toDimKey.
     * Replaces the target values. There is no consistency check (e.g. moving a numeric range to the iri dimension)
     * Use with care.
     */
    // TODO Think of a better/safer/validating way
    ValueSpace moveDimension(Object fromDimKey, Object toDimKey);

    /** Whether the value space only has ranges in the dimension with the given key (e.g. numeric, iRI, text, ...) */
    boolean isLimitedTo(Object dimensionKey);

    /** Whether the set of values described by this value space is empty.
     * Technically an alias for isConflicting */
    default boolean isEmpty() {
        return isConflicting();
    }
}
