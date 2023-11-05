package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

public class ElementGeneratorUtils {


    public static SetMultimap<FacetPath, Expr> indexConstraints(Collection<Expr> constraints) {
        SetMultimap<FacetPath, Expr> result = LinkedHashMultimap.create();
        for(Expr expr : constraints) {
            Set<FacetPath> paths = NodeCustom.mentionedValues(FacetPath.class, expr);
            for(FacetPath path : paths) {
                result.put(path, expr);
            }
        }
        return result;
    }

    public static <P> SetMultimap<P, Expr> hideConstraintsForPath(SetMultimap<P, Expr> constaintIndex, P path) {
        SetMultimap<P, Expr> result = Multimaps.filterKeys(constaintIndex, k -> !Objects.equals(k, path));
        return result;
    }

//    public static <T, P extends Path<T>> T getSegmentAt(P path, int index) {
//        T result = index < path.getNameCount() ? path.getName(index).toSegment() : null;
//        return result;
//    }


    /**
     * Remove the first segment from a path if it does not generate a sparql graph pattern.
     *
     */
    public static FacetPath cleanPath(FacetPath path) {
        FacetPath firstLink = path.getNameCount() > 0 ? path.getName(0) : null;
        FacetPath result = path;
        if (firstLink != null) {
            FacetStep step = firstLink.toSegment();
            if (step.getNode().equals(FacetedRelationQuery.INITIAL_VAR)) {
                result = firstLink.relativize(path);
            }
        }
        return result;
    }

}
