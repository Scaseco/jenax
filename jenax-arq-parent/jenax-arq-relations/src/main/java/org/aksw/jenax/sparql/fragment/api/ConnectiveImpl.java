package org.aksw.jenax.sparql.fragment.api;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public class ConnectiveImpl
    implements Connective
{
    protected Fragment fragment;
    protected List<Var> vars;

    protected ConnectiveImpl(Fragment fragment, List<Var> vars) {
        super();
        this.fragment = fragment;
        this.vars = vars;
    }

    public static Connective of(Fragment fragment, Var...vars) {
        return of(fragment, Arrays.asList(vars));

    }
    public static Connective of(Fragment fragment, List<Var> vars) {
        return new ConnectiveImpl(fragment, vars);
    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public List<Var> getVars() {
        return vars;
    }
}
