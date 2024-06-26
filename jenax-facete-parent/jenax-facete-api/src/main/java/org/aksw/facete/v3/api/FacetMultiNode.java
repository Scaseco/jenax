package org.aksw.facete.v3.api;

import org.aksw.facete.v3.api.traversal.TraversalMultiNode;

public interface FacetMultiNode extends TraversalMultiNode<FacetNode> {


    /** True iff multiple aliases are referenced in constraints */
    boolean hasMultipleReferencedAliases();

    boolean contains(FacetNode facetNode);

    /**
     * Yield the set of values that remain after application of the constraints.
     * This is the set of values that can be added to a new instance of the property.
     *
     * By default, excludes values that have been set as constraints.
     *
     */
    void remainingValues();


    void availableValues();
}
