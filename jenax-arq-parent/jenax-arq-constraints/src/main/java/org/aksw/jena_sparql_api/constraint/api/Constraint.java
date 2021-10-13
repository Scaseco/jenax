package org.aksw.jena_sparql_api.constraint.api;

public interface Constraint
    extends Contradictable
{
    boolean stateUnion(Constraint other);
    boolean stateIntersection(Constraint other);
}
