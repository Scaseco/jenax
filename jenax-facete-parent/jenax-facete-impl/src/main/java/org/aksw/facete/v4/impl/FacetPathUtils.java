package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;

public class FacetPathUtils {

    public static FacetStep toElementId(FacetStep step) {
        return FacetStep.isTuple(step.getTargetComponent())
                ? step
                : new FacetStep(step.getNode(), step.isForward(), step.getAlias(), FacetStep.TUPLE);
    }

    /**
     * Replace the last step of a FacetPath with one that refers to the graph pattern itself.
     */
    public static FacetPath toElementId(FacetPath path) {
        FacetPath result = path.getParent() == null
                ? path
                : path.resolveSibling(toElementId(path.getFileName().toSegment()));
        return result;
    }

    /**
     * Given a pool of paths, return those that are a direct successor of 'basePath' in the specified direction.
     */
    public static Set<FacetPath> getDirectChildren(FacetPath basePath, Direction direction, Collection<FacetPath> pool) {
        boolean isForward = direction.isForward();
        Set<FacetPath> result = new LinkedHashSet<>();
        for(FacetPath cand : pool) {
            FacetPath candParent = cand.getParent();

            // candParent must neither be null nor the root, otherwise isReverse will throw an exception
            if(candParent != null) {
                boolean isCandFwd = cand.getFileName().toSegment().isForward();

                if(isForward == isCandFwd && Objects.equals(basePath, candParent)) {
                    result.add(cand);
                }
            }
        }

        return result;
    }

    /** Adaption for scoped facet paths */
    public static Set<ScopedFacetPath> getDirectChildren(ScopedFacetPath basePath, Direction direction, Collection<ScopedFacetPath> pool) {
        boolean isForward = direction.isForward();
        Set<ScopedFacetPath> result = new LinkedHashSet<>();
        for(ScopedFacetPath cand : pool) {
            ScopedFacetPath candParent = cand.getParent();

            // candParent must neither be null nor the root, otherwise isReverse will throw an exception
            if(candParent != null) {
                boolean isCandFwd = cand.getFacetPath().getFileName().toSegment().isForward();

                if(isForward == isCandFwd && Objects.equals(basePath, candParent)) {
                    result.add(cand);
                }
            }
        }

        return result;
    }

}
