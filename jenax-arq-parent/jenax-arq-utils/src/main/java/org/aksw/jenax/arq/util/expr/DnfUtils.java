package org.aksw.jenax.arq.util.expr;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

public class DnfUtils {


    public static Expr toExpr(Iterable<? extends Iterable<Expr>> ors) {
        List<Expr> tmpOrs = new ArrayList<Expr>();
        for(Iterable<Expr> ands : ors) {
            Expr and = ExprUtils.andifyBalanced(ands);
            if(and == null) {
                and = NodeValue.TRUE;
            }

            tmpOrs.add(and);
        }

        if(Iterables.isEmpty(tmpOrs)) {
            return NodeValue.FALSE;
        }

        Expr result = ExprUtils.orifyBalanced(tmpOrs);

        return result;
    }

}
