package org.aksw.jenax.constraint.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.constraint.api.CBinding;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ValueSpace;

public class CBindingMap
    implements CBinding
{
    protected Map<Var, VSpace> varToProfile;

    protected CBindingMap(Map<Var, VSpace> varToProfile) {
        super();
        this.varToProfile = varToProfile;
    }

    public static CBinding create() {
        return new CBindingMap(new HashMap<>());
    }

    @Override
    public Collection<Var> getVars() {
        return varToProfile.keySet();
    }

    @Override
    public VSpace get(Var var) {
        return varToProfile.get(var);
    }

    @Override
    public String toString() {
        return varToProfile.toString();
    }

    @Override
    public CBinding cloneObject() {
        Map<Var, VSpace> map = varToProfile.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().clone()));
        return new CBindingMap(map);
    }

    @Override
    public boolean isConflicting() {
        boolean result = varToProfile.values().stream().anyMatch(VSpace::isConflicting);
        return result;
    }

    @Override
    public CBinding stateIntersection(CBinding that) {
        Set<Var> vars = new HashSet<>();
        vars.addAll(getVars());
        vars.addAll(that.getVars());

        for (Var var : vars) {
            VSpace thatSpace = that.get(var);
            stateIntersection(var, thatSpace);
        }

        return this;
    }

    VSpace vsUndef = VSpaceImpl.create(NodeRanges.createClosed().addOpenDimension(ValueSpace.VSPACE_UNDEF));

    @Override
    public CBinding stateUnion(CBinding that) {
        Set<Var> allVars = new LinkedHashSet<>();
        allVars.addAll(this.getVars());
        allVars.addAll(that.getVars());
        for (Var var : allVars) {
            VSpace thisSpace = this.get(var);
            if (thisSpace == null) {
                this.stateIntersection(var, vsUndef);
            }

            VSpace thatSpace = that.get(var);
            stateUnion(var, thatSpace);
        }

        return this;
    }

    @Override
    public CBinding stateIntersection(Var var, VSpace thatSpace) {
        VSpace thisSpace = varToProfile.get(var);

        if (thisSpace == null) {
            varToProfile.put(var, thatSpace.clone());
        } else if (thatSpace != null) {
            thisSpace.stateIntersection(thatSpace);
        } // else if thatSpace is null then nothing to do

        return this;
    }

    @Override
    public CBinding stateUnion(Var var, VSpace thatSpace) {
        VSpace thisSpace = varToProfile.get(var);

        if (thisSpace == null) {
            varToProfile.put(var, thatSpace.clone());
        } else if (thatSpace != null) {
            thisSpace.stateUnion(thatSpace);
        } // else if thatSpace is null then nothing to do

        return this;
    }

    @Override
    public CBinding project(Collection<Var> vars) {
        varToProfile.keySet().retainAll(vars);
        return this;
    }

}
