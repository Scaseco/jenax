package org.aksw.jenax.facete.treequery2.api;

/**
 * This interface features support for traversals along facet steps
 * rooted in an object of type R.
 *
 * @param <R>
 */
public interface RootedFacetTraversable<R, T extends RootedFacetTraversable<R, T>>
    extends FacetTraversable<T>
{
    R getRoot();
}
