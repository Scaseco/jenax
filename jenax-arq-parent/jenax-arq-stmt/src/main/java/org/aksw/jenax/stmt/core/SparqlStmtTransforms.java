package org.aksw.jenax.stmt.core;

import java.util.function.Supplier;

import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class SparqlStmtTransforms {
    public static SparqlStmtTransform of(Transform statelessTransform) {
        return of(op -> Transformer.transform(statelessTransform, op));
    }

    public static SparqlStmtTransform of(Supplier<Transform> transformSupplier) {
        return of(op -> Transformer.transform(transformSupplier.get(), op));
    }
    public static SparqlStmtTransform ofExprTransform(Supplier<? extends ExprTransform> transformSupplier) {
        return of(op -> Transformer.transform(null, transformSupplier.get(), op));
    }

    public static SparqlStmtTransform ofExprTransform(ExprTransform exprTransform) {
        return of(op -> Transformer.transform(null, exprTransform, op));
    }

    public static SparqlStmtTransform of(Rewrite rewrite) {
        return new SparqlStmtTransformViaRewrite(rewrite);
    }
}
