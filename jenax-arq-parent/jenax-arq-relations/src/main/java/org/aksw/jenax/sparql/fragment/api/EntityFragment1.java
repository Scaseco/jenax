package org.aksw.jenax.sparql.fragment.api;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class EntityFragment1
    extends EntityFragmentBase
{
    protected Var entityVar;

    protected EntityFragment1(Fragment fragment, Var entityVar) {
        super(fragment);
        this.entityVar = entityVar;
    }

    @Override
    public Set<Var> getEntityVars() {
        return Set.of(entityVar);
    }
}
