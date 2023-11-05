package org.aksw.facete.v3.api;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public interface HLFacetConstraint<B> {
    FacetConstraintControl state();
    Map<Node, FacetNode> mentionedFacetNodes();

    Expr expr();


    boolean isActive();

    default boolean setActive(boolean onOrOff) {
        boolean isActive = isActive();
        if(onOrOff) {
            setActive();
        } else {
            remove();
        }

        // Return true if there was a change
        boolean result = isActive != onOrOff;
        return result;
    }


    boolean setActive();
    boolean remove();
//	HLFacetConstraint<B> remove();

    default B activate() {
        setActive(true);
        return parent();
    }

    default B deactivate() {
        setActive(false);
        return parent();
    }

    /**
     *
     * @return Active state after toggle
     */
    default HLFacetConstraint<B> toggle() {
        boolean result = !isActive();
        setActive(result);
        //return result;
        return this;
    }



    B parent();
}
