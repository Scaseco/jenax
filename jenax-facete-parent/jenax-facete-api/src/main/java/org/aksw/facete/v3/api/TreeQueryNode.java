package org.aksw.facete.v3.api;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.path.core.HasFacetPath;

/**
 * A node in a tree structure from which {@link FacetPath} instances are derived.
 * The underlying tree structure can be modified which may change the result of {@link #getFacetPath()}.
 * The typical transformation is rotating the tree such that another node becomes its root using {@link #chRoot()}.
 * Instances of this class can be wrapped as Jena Nodes using {@link NodeFacetPath} which allows them to use in expressions.
 */
public interface TreeQueryNode
    extends HasFacetPath
{
    TreeQuery getTree();

    /**
     * Rotate the tree such that this node becomes its root.
     * This means, that if this node has a parent then it will become a child of this node.
     */
    void chRoot();

    FacetStep reachingStep();
    TreeQueryNode getParent();

    TreeQueryNode getOrCreateChild(FacetStep facetStep);

    /** Resolves a path, calls getOrCreateChild on nodes as needed */
    TreeQueryNode resolve(FacetPath facetPath);
}
