package org.aksw.jenax.arq.util.op;

import java.util.Objects;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.expr.ExprTransform;

public class OpTransformFromExprTransform
    implements OpTransform
{
    protected ExprTransform exprTransform;

    public OpTransformFromExprTransform(ExprTransform exprTransform) {
        super();
        this.exprTransform = Objects.requireNonNull(exprTransform);
    }

    @Override
    public Op apply(Op t) {
        Op result = Transformer.transform(null, exprTransform, t);
        return result;
    }
}
