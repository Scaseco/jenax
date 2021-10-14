package org.aksw.jenax.constraint.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.api.ValueSpace;
import org.apache.jena.sparql.core.Var;

public class ConstraintRowMap
    implements ConstraintRow
{
    protected Map<Var, ValueSpace> varToProfile = new HashMap<>();

    @Override
    public Collection<Var> getVars() {
        return varToProfile.keySet();
    }

    @Override
    public ValueSpace get(Var var) {
        return varToProfile.get(var);
    }

    public ConstraintRowMap(Map<Var, ValueSpace> varToProfile) {
        super();
        this.varToProfile = varToProfile;
    }

    @Override
    public String toString() {
        return "ConstraintRowMap [varToProfile=" + varToProfile + "]";
    }

    @Override
    public boolean isConflicting() {
        boolean result = varToProfile.values().stream().anyMatch(ValueSpace::isConflicting);
        return result;
    }

    @Override
    public ConstraintRow stateIntersection(ConstraintRow that) {
        Set<Var> vars = new HashSet<>();
        vars.addAll(getVars());
        vars.addAll(that.getVars());

        for (Var var : vars) {
            ValueSpace thisSpace = varToProfile.get(var);
            ValueSpace thatSpace = that.get(var);

            if (thisSpace == null) {
                varToProfile.put(var, thatSpace.clone());
            } else if (thatSpace != null) {
                thisSpace.stateIntersection(thatSpace);
            } // else if thatSpace is null then nothing to do
        }

        return this;
    }

    @Override
    public ConstraintRow stateUnion(ConstraintRow that) {
        for (Var var : that.getVars()) {
            ValueSpace thisSpace = varToProfile.get(var);
            ValueSpace thatSpace = that.get(var);

            if (thisSpace == null) {
                varToProfile.put(var, thatSpace.clone());
            } else if (thatSpace != null) {
                thisSpace.stateUnion(thatSpace);
            } // else if thatSpace is null then nothing to do

        }

        return this;
    }
}
