package org.aksw.jenax.graphql.sparql.v2.util;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;

public class ExprUtils {
    /**
     * Node transform version that
     * (a) handles blank nodes correctly; in constrast to Expr.applyNodeTransform
     * [disabled (b) treats null mappings as identity mapping]
     *
     */
    public static Expr applyNodeTransform(Expr expr, NodeTransform xform) {
        Expr result = ExprTransformer.transform(new NodeTransformExpr(node -> {
            Node r = xform.apply(node);
            //Node r = Optional.ofNullable(xform.apply(node)).orElse(node);
            return r;
        }), expr);
        return result;
    }

}
