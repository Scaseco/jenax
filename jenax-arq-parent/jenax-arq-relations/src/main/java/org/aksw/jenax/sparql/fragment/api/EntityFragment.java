package org.aksw.jenax.sparql.fragment.api;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * Combines a fragment (graph pattern plus exposed variables) with additional information about
 * which of the underlying graph pattern's visible variables form a composite key for entities.
 */
public interface EntityFragment {
    Fragment getFragment();
    Set<Var> getEntityVars();

    default EntityFragment1 asEntityFragement1() {
        return (EntityFragment1)this;
    }

    static EntityFragment of(Fragment fragment, Var var) {
        return new EntityFragment1(fragment, var);
    }

    static EntityFragment of(Fragment fragment, Var ... vars) {
        return vars.length == 1 ? of(fragment, vars[0]) : of(fragment, new LinkedHashSet<>(Arrays.asList(vars)));
    }

    static EntityFragment of(Fragment fragment, Set<Var> entityVars) {
        return entityVars.size() == 1
            ? of(fragment, entityVars.iterator().next())
            : new EntityFragmentN(fragment, entityVars);
    }
}
