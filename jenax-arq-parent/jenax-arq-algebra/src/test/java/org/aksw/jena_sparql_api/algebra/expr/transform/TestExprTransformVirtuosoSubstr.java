package org.aksw.jena_sparql_api.algebra.expr.transform;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;


public class TestExprTransformVirtuosoSubstr {
    @Test
    public void test_01() {
        Expr actualExpr = ExprTransformer.transform(new ExprTransformVirtuosoSubstr(),
                ExprUtils.parse("substr('hello', 3, 10)"));
        NodeValue actualValue = ExprUtils.eval(actualExpr);
        Assert.assertEquals("llo", actualValue.getString());
        // System.err.println(ExprUtils.fmtSPARQL(actual));
    }
}
