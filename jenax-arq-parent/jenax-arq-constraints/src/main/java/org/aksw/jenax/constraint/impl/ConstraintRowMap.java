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
    protected Map<Var, ValueSpace> varToProfile;

    protected ConstraintRowMap(Map<Var, ValueSpace> varToProfile) {
        super();
        this.varToProfile = varToProfile;
    }

    public static ConstraintRow create() {
        return new ConstraintRowMap(new HashMap<>());
    }

    @Override
    public Collection<Var> getVars() {
        return varToProfile.keySet();
    }

    @Override
    public ValueSpace get(Var var) {
        return varToProfile.get(var);
    }

    @Override
    public String toString() {
        return varToProfile.toString();
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
            ValueSpace thatSpace = that.get(var);
            stateIntersection(var, thatSpace);
        }

        return this;
    }

    @Override
    public ConstraintRow stateUnion(ConstraintRow that) {
        for (Var var : that.getVars()) {
            ValueSpace thatSpace = that.get(var);
            stateUnion(var, thatSpace);
        }

        return this;
    }

    @Override
    public ConstraintRow stateIntersection(Var var, ValueSpace thatSpace) {
        ValueSpace thisSpace = varToProfile.get(var);

        if (thisSpace == null) {
            varToProfile.put(var, thatSpace.clone());
        } else if (thatSpace != null) {
            thisSpace.stateIntersection(thatSpace);
        } // else if thatSpace is null then nothing to do

        return this;
    }

    @Override
    public ConstraintRow stateUnion(Var var, ValueSpace thatSpace) {
        ValueSpace thisSpace = varToProfile.get(var);

        if (thisSpace == null) {
            varToProfile.put(var, thatSpace.clone());
        } else if (thatSpace != null) {
            thisSpace.stateUnion(thatSpace);
        } // else if thatSpace is null then nothing to do

        return this;
    }

    @Override
    public ConstraintRow project(Collection<Var> vars) {
        varToProfile.keySet().retainAll(vars);
        return this;
    }

}
