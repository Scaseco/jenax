package org.aksw.jenax.constraint.api;

public interface Constrainable
    extends Cloneable
{
    Constrainable clone();

    Constrainable stateIntersection(ValueSpace valueSpace);
    Constrainable stateUnion(ValueSpace valueSpace);
    boolean isConflicting();
}
