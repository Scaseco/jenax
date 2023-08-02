package org.aksw.jenax.sparql.qudtlib;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;

public class NoSuchUnitException extends ExprEvalException {
    public NoSuchUnitException(Node node) {
        super("Unit not found: " + node);
    }
}
