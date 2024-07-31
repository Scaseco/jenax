package org.aksw.jenax.facete.treequery2.api;

import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v4.impl.PropertyResolver;
import org.apache.jena.sparql.core.Var;

public interface QueryContext {
    /** Declared variable names that are already in use. May be used to avoid name clashes. */
    Set<Var> getUsedVars();

    /**
     * Scope id generator.
     * Used to allocate a fresh ID for each relation attached to the query model.
     *
     * @deprecated Probably no longer needed, because scope names can be deterministically inferred from paths over the fields of the name query.
     *             Perhaps a scope prefix could still be useful to mitigate potential clashes with allocated names.
     */
    @Deprecated
    Generator<String> getScopeNameGenerator(); // Some code still uses this

    /** Mapping of facet paths to unique variable names. */
    FacetPathMapping getPathMapping();

    /** Mapping of virtual properties to their defining graph pattern. */
    PropertyResolver getPropertyResolver();
}
