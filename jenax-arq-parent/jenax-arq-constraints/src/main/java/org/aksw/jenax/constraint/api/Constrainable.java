package org.aksw.jenax.constraint.api;

/** Interface for entities that can be constrained by value spaces */
public interface Constrainable
    extends Cloneable
{
    Constrainable clone();

    Constrainable stateIntersection(VSpace valueSpace);
    Constrainable stateUnion(VSpace valueSpace);
    boolean isConflicting();
}
