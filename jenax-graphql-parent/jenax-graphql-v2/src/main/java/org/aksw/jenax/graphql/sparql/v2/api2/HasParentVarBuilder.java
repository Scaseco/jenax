package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface HasParentVarBuilder<T extends HasParentVarBuilder<T>>
    extends HasSelf<T>
{
    default T parentVarNames(String...varNames) {
        List<String> list = Arrays.asList(varNames);
        parentVarNames(list);
        return self();
    }

    default  T parentVarNames(List<String> varNames) {
        List<Var> list = varNames == null ? null : Var.varList(varNames);
        parentVars(list);
        return self();
    }

    default  T parentVars(Var ... vars) {
        List<Var> list = Arrays.asList(vars);
        parentVars(list);
        return self();
    }

    default T parentVars(List<Var> vars) {
        setParentVars(vars);
        return self();
    }

    void setParentVars(List<Var> vars);
}
