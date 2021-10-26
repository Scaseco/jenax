package org.aksw.jenax.arq.util.node;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class RangeUtils {

    /** Create an (in)equality expression from the given node (typically a variable) and range.
     * For example, for the arguments (?x, [5, 10)) the result is ?x >= 5 && ?x < 10.
     */
    public static Expr createExpr(Node node, Range<? extends ComparableNodeValue> range) {
        Expr n = ExprLib.nodeToExpr(node);

        List<Expr> parts = new ArrayList<>();

        if (org.aksw.commons.util.range.RangeUtils.isSingleton(range)) {
            parts.add(new E_Equals(n, ExprLib.nodeToExpr(range.lowerEndpoint().getNode())));
        } else {

            if(range.hasLowerBound()) {
                if(range.lowerBoundType().equals(BoundType.OPEN)) {
                    parts.add(new E_GreaterThan(n, range.lowerEndpoint().getNodeValue()));
                } else {
                    parts.add(new E_GreaterThanOrEqual(n, range.lowerEndpoint().getNodeValue()));
                }
            }

            if(range.hasUpperBound()) {
                if(range.upperBoundType().equals(BoundType.OPEN)) {
                    parts.add(new E_LessThan(n, range.upperEndpoint().getNodeValue()));
                } else {
                    parts.add(new E_LessThanOrEqual(n, range.upperEndpoint().getNodeValue()));
                }
            }
        }

        Expr result = ExprUtils.andifyBalanced(parts);
        return result;
    }
}
