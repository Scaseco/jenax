package org.aksw.jenax.facete.treequery2.api;

import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v4.impl.PropertyResolver;
import org.apache.jena.sparql.core.Var;

public interface QueryContext {
    Set<Var> getUsedVars();

    /**
     * Field id generator.
     * Used to allocate a fresh ID for each relation attached to the query model
     */
    @Deprecated // Probably no longer needed?
    Generator<String> getFieldIdGenerator();

    FacetPathMapping getPathMapping();
    PropertyResolver getPropertyResolver();
}
