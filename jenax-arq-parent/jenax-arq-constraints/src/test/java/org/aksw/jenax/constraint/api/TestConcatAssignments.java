package org.aksw.jenax.constraint.api;

import java.util.Arrays;
import java.util.List;

import org.aksw.jenax.sparql.expr.optimize.util.Alignment;
import org.aksw.jenax.sparql.expr.optimize.util.StringAlignments;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

public class TestConcatAssignments {

    @Test
    public void testExpr() {
//        Expr r = ConcatAssignments.optimizeRdfTerm(new E_Equals(
//                E_StrConcatPermissive.create(NodeValue.makeString("<"), new ExprVar("p"), NodeValue.makeString(">")),
//                NodeValue.makeString("<foo>")));

        List<Alignment> r = StringAlignments.align(
                Arrays.<Expr>asList(NodeValue.makeString("<"), new ExprVar("x"), NodeValue.makeString("-"), new ExprVar("y"), NodeValue.makeString(">")),
                Arrays.<Expr>asList(NodeValue.makeString("<foo-b-ar>")));

        System.out.println(r);
    }
}
