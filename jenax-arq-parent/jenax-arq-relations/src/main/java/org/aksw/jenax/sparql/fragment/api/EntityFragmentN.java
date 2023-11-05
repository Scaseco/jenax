package org.aksw.jenax.sparql.fragment.api;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class EntityFragmentN
    extends EntityFragmentBase
{
    protected Set<Var> entityVars;

    protected EntityFragmentN(Fragment fragment, Set<Var> entityVars) {
        super(fragment);
        this.entityVars = entityVars;
    }

    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public Set<Var> getEntityVars() {
        return entityVars;
    }

}
