package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface HasTargetVarBuilder<T extends HasTargetVarBuilder<T>>
    extends HasSelf<T>
{
    default T targetVarNames(String...varNames) {
        List<String> list = Arrays.asList(varNames);
        targetVarNames(list);
        return self();
    }

    default  T targetVarNames(List<String> varNames) {
        List<Var> list = varNames == null ? null : Var.varList(varNames);
        targetVars(list);
        return self();
    }

    default  T targetVars(Var ... vars) {
        List<Var> list = Arrays.asList(vars);
        targetVars(list);
        return self();
    }

    default T targetVars(List<Var> vars) {
        setTargetVars(vars);
        return self();
    }

    void setTargetVars(List<Var> vars);
}
