package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;

/**
 * This implementation features support for traversals along facet paths
 * as well as setting constraints on the paths.
 */
public class ConstraintNodeImpl
    extends RootedFacetTraversableBase<NodeQuery, ConstraintNode<NodeQuery>>
    implements ConstraintNode<NodeQuery>
{
    public ConstraintNodeImpl(NodeQuery root, FacetPath path) {
        super(root, path);
    }

    @Override
    public ConstraintNode<NodeQuery> getParent() {
        FacetPath parentPath = facetPath.getParent();
        return parentPath == null ? null : new ConstraintNodeImpl(root, parentPath);
    }

    @Override
    public ConstraintNodeImpl getOrCreateChild(FacetStep step) {
        FacetPath newPath = facetPath.resolve(step);
        return new ConstraintNodeImpl(root, newPath);
    }

    @Override
    public ConstraintFacade<ConstraintNode<NodeQuery>> enterConstraints() {
    	FacetConstraints<ConstraintNode<NodeQuery>> facetConstraints = root.relationQuery().getFacetConstraints();    	
        ConstraintApi2Impl<ConstraintNode<NodeQuery>> constraintView = new ConstraintApi2Impl<>(facetConstraints, this);
        return new ConstraintFacade2Impl<>(this, constraintView);
    }    
}
