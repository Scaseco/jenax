package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.jenax.facete.treequery2.api.RootedFacetTraversable;
import org.aksw.jenax.path.core.FacetPath;

import com.google.common.base.Objects;

public abstract class RootedFacetTraversableBase<R, T extends RootedFacetTraversable<R, T>>
    implements RootedFacetTraversable<R, T>
{
    /** Roots are compared using reference equality! */
    protected R root;
    protected FacetPath facetPath;

    public RootedFacetTraversableBase(R root, FacetPath path) {
        super();
        this.root = root;
        this.facetPath = path;
    }

//    @Override
//    public T getParent() {
//        FacetPath parentPath = facetPath.getParent();
//        return parentPath == null ? null : new Roo
//    }

    @Override
    public R getRoot() {
        return root;
    }

    @Override
    public FacetPath getFacetPath() {
        return facetPath;
    }

//    @Override
//    public RootedFacetTraversableImpl<R> getOrCreateChild(FacetStep step) {
//        FacetPath newPath = path.resolve(step);
//        return new RootedFacetTraversableImpl<>(root, newPath);
//    }
//
//    protected T createChild(FacetStep step) {
//
//    }

    @Override
    public int hashCode() {
        return Objects.hashCode(root, facetPath);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof RootedFacetTraversableBase) {
            RootedFacetTraversableBase<?, ?> o = (RootedFacetTraversableBase<?, ?>)obj;
            result = o.root == root && o.facetPath.equals(facetPath);
        }
        return result;
    }
}
