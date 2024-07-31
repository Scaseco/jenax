package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface HasConnectVarBuilder<T extends HasConnectVarBuilder<T>>
    extends HasSelf<T>
{
    default T connectVarNames(String...varNames) {
        List<String> list = Arrays.asList(varNames);
        connectVarNames(list);
        return self();
    }

    default  T connectVarNames(List<String> varNames) {
        List<Var> list = varNames == null ? null : Var.varList(varNames);
        connectVars(list);
        return self();
    }

    default  T connectVars(Var ... vars) {
        List<Var> list = Arrays.asList(vars);
        connectVars(list);
        return self();
    }

    default T connectVars(List<Var> vars) {
        setConnectVars(vars);
        return self();
    }

    void setConnectVars(List<Var> vars);
}
