package org.aksw.facete.v3.api;

import org.apache.jena.sparql.expr.Expr;

/** Interface to toggle a single constraint on or off. */
public interface FacetConstraintCore {
    boolean enabled();
    FacetConstraintCore enabled(boolean onOrOff);

    Expr expr();
}
