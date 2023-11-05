package org.aksw.facete.v3.api;

import org.apache.jena.sparql.expr.Expr;

/** Interface to toggle an individual constraint on or off or remove it alltogether */
public interface FacetConstraintControl {
    boolean enabled();
    FacetConstraintControl enabled(boolean onOrOff);

    /** Get the constraint expression (the actual constraint payload) */
    Expr expr();

    void unlink();
}
