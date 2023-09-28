package org.aksw.jenax.facete.treequery2.api;

public interface OrderNode<R>
    extends RootedFacetTraversable<R, OrderNode<R>>
{
    /**
     * Return the NodeQuery which is the root of this traversal
     */
    // R getStartNode();

    R asc();
    R desc();
}
