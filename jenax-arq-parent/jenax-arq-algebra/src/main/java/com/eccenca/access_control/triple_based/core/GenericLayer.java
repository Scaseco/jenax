package com.eccenca.access_control.triple_based.core;

import java.util.List;

import org.aksw.commons.collections.PolaritySet;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.XExpr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class GenericLayer {
    protected List<Var> vars;
    protected Fragment relation;
    protected XExpr expr;
    protected PolaritySet<Binding> valueSet;

    public GenericLayer(Fragment relation) {
        this.relation = relation;
    }

    public List<Var> getVars() {
        return vars;
    }
    public void setVars(List<Var> vars) {
        this.vars = vars;
    }
    public Fragment getRelation() {
        return relation;
    }
    public void setRelation(Fragment relation) {
        this.relation = relation;
    }
    public XExpr getExpr() {
        return expr;
    }
    public void setExpr(XExpr expr) {
        this.expr = expr;
    }
    public PolaritySet<Binding> getValueSet() {
        return valueSet;
    }
    public void setValueSet(PolaritySet<Binding> valueSet) {
        this.valueSet = valueSet;
    }

    public static GenericLayer create(Fragment relation) {
        return new GenericLayer(relation);
    }


}
