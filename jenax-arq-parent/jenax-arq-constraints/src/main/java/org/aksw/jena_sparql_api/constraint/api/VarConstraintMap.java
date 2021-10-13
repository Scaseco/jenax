package org.aksw.jena_sparql_api.constraint.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.sparql.core.Var;

import com.google.common.collect.ForwardingMap;

/** A map from vars to constraints */
public class VarConstraintMap
    extends ForwardingMap<Var, Constraint>
    implements VarConstraint
{
    protected Map<Var, Constraint> delegate = new LinkedHashMap<>();

    @Override
    protected Map<Var, Constraint> delegate() {
        return delegate;
    }

}
