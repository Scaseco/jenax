package org.aksw.jenax.facete.treequery2.api;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.jenax.path.core.FacetPath;

public interface ConstraintNode<R>
    extends RootedFacetTraversable<R, ConstraintNode<R>>, Sortable<ConstraintNode<R>>
{
    ConstraintFacade<? extends ConstraintNode<R>> enterConstraints();

    public static ScopedFacetPath toScopedFacetPath(ConstraintNode<NodeQuery> constraintNode) {
        NodeQuery nodeQuery = constraintNode.getRoot();
        FacetPath constraintPath = constraintNode.getFacetPath();
        ScopedFacetPath result = NodeQuery.toScopedFacetPath(nodeQuery, constraintPath);
        return result;
    }
}
