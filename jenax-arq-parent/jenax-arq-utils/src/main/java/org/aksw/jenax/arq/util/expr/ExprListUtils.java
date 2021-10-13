package org.aksw.jenax.arq.util.expr;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;

public class ExprListUtils {
    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr e = ExprLib.nodeToExpr(node);
            result.add(e);
        }

        return result;
    }
}
