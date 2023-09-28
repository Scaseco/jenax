package org.aksw.jenax.facete.treequery2.api;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.path.core.HasFacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Base interface for traversals along FacetSteps.
 *
 * @param <T>
 */
public interface FacetTraversable<T extends FacetTraversable<T>>
    extends HasFacetPath
{
    default T fwd(String property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T fwd(Node property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T fwd(Resource property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T bwd(String property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default T bwd(Node property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default T bwd(Resource property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    /** Returns null if there is no child reachable with the given step. */
    // RootNode getChild(FacetStep step);
    T getOrCreateChild(FacetStep step);

    T getParent();

    default T getRootNode() {
        return TreeUtils.getRoot((T)this, FacetTraversable::getParent);
    }

    default T resolve(FacetPath facetPath) {
        return FacetTraversable.resolve((T)this, facetPath);
    }

    public static <T extends FacetTraversable<T>> T resolve(T node, FacetPath facetPath) {
        T tmp = facetPath.isAbsolute() ? node.getRootNode() : node;
        for (FacetStep step : facetPath.getSegments()) {
            if (FacetPathOps.PARENT.equals(step)) {
                tmp = tmp.getParent();
            } else if (FacetPathOps.SELF.equals(step)) {
                // Nothing to do
            } else {
                tmp = tmp.getOrCreateChild(step);
            }
        }
        return tmp;
    }

}
