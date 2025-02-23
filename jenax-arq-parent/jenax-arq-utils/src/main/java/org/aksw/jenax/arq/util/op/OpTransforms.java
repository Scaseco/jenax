package org.aksw.jenax.arq.util.op;

import org.apache.jena.sparql.expr.ExprTransform;

public class OpTransforms {
    public static OpTransform of(ExprTransform transform) {
        return new OpTransformFromExprTransform(transform);
    }
}
