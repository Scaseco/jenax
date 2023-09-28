package org.aksw.jenax.facete.treequery2.api;

import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v4.impl.PropertyResolver;
import org.apache.jena.sparql.core.Var;

public interface QueryContext {
    Set<Var> getUsedVars();

    @Deprecated // Probably no longer needed?
    Generator<String> getScopeNameGenerator();

    FacetPathMapping getPathMapping();
    PropertyResolver getPropertyResolver();
}
