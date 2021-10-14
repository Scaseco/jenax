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


    /** Whether the set of values described by this value space is empty.
     * Technically an alias for isConflicting */
    default boolean isEmpty() {
        return isConflicting();
    }
}
