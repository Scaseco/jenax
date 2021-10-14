package org.aksw.jenax.constraint.api;

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
