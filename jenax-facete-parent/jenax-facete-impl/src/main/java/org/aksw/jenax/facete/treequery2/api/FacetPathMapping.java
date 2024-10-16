package org.aksw.jenax.facete.treequery2.api;

import org.aksw.jenax.path.core.FacetPath;

/**
 * A mapping of FacetPaths to strings, typically hashes.
 * The returned strings should be valid SPARQL variable names.
 *
 * The mapping must be injective so no distinct paths may map to the same variable.
 * Formally, injective means that if f(x) equals f(y) then it follows that x equals y.
 */
public interface FacetPathMapping {
    String allocate(FacetPath facetPath);
}
