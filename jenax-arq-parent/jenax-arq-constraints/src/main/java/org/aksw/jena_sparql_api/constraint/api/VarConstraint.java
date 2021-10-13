package org.aksw.jena_sparql_api.constraint.api;

import java.util.Map;

import org.apache.jena.sparql.core.Var;

public interface VarConstraint
    extends Map<Var, Constraint>
{
}
