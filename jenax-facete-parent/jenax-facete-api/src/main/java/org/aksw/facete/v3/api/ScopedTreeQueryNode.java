package org.aksw.facete.v3.api;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;

public interface ScopedTreeQueryNode {
    ScopedTreeQuery getTree();

    /**
     * Rotate the tree such that this node becomes its root.
     * This means, that if this node has a parent then it will become a child of this node.
     */
    void chRoot();

    FacetStep reachingStep();
    ScopedTreeQueryNode getParent();

    ScopedTreeQueryNode getOrCreateChild(FacetStep facetStep);

    /** Resolves a path, calls getOrCreateChild on nodes as needed */
    ScopedTreeQueryNode resolve(FacetPath facetPath);
}
