package org.aksw.jenax.arq.util.expr;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

public class FilterUtils {
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

}
