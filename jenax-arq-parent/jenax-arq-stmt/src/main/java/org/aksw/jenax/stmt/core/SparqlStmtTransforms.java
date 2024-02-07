package org.aksw.jenax.stmt.core;

import java.util.function.Supplier;

import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.expr.ExprTransform;

public class SparqlStmtTransforms {
    public static SparqlStmtTransform of(Transform statelessTransform) {
        SparqlStmtTransform result =
                stmt -> SparqlStmtUtils.applyOpTransform(stmt,
                    op -> Transformer.transform(statelessTransform, op));
        return result;
    }

    public static SparqlStmtTransform of(Supplier<Transform> transformSupplier) {
        SparqlStmtTransform result =
                stmt -> SparqlStmtUtils.applyOpTransform(stmt,
                    op -> Transformer.transform(transformSupplier.get(), op));
        return result;
    }

    public static SparqlStmtTransform ofExprTransform(Supplier<? extends ExprTransform> transformSupplier) {
        SparqlStmtTransform result =
                stmt -> SparqlStmtUtils.applyOpTransform(stmt,
                    op -> Transformer.transform(null, transformSupplier.get(), op));
        return result;
    }
}
