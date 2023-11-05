package org.aksw.facete.v3.api;

import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

/**
 * @deprecated Superseded by {@link org.aksw.jenax.facete.treequery2.impl.FacetPathMapping} in the refactored API
 * <p>
 * A mapping of FacetPaths to variables.
 * The mapping must be injective so no distinct paths may map to the same variable.
 * Formally, injective means that if f(x) = f(y) then it follows that x = y.
 *
 * The purpose of this class is to map paths consistently to variables.
 */
@Deprecated
public interface FacetPathMapping {
    Var allocate(FacetPath facetPath);
}
