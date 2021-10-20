package org.aksw.jenax.arq.util.expr;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;

public class ExprListUtils {

    /** Apply ExprLib.nodeToExpr for an iterable of nodes */
    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr e = ExprLib.nodeToExpr(node);
            result.add(e);
        }

        return result;
    }

    /** Convert a list of exprlists into a set of sets */
    public static Set<Set<Expr>> toSets(List<ExprList> clauses)
    {
        if(clauses == null) {
            return null;
        }

        Set<Set<Expr>> result = new LinkedHashSet<Set<Expr>>();

        for(ExprList clause : clauses) {
            result.add(new LinkedHashSet<Expr>(clause.getList()));
        }

        return result;
    }


    public static boolean contains(ExprList exprList, Expr expr) {
        boolean result = false;

        for(Expr item : exprList) {
            result = item.equals(expr);
            if(result) {
                break;
            }
        }

        return result;
    }

}
