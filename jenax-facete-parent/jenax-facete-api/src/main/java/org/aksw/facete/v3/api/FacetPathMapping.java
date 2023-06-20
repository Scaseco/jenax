package org.aksw.facete.v3.api;

import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

/**
 * A mapping of FacetPaths to variables. The mapping must be injective so no distinct paths may map to the same variable.
 * Formally: from f(x) = f(y) it follows that x = y.
 *
 * The purpose of this class is to map paths consistently to variables.
 */
public interface FacetPathMapping {
    Var allocate(FacetPath treeQueryNode);
}
