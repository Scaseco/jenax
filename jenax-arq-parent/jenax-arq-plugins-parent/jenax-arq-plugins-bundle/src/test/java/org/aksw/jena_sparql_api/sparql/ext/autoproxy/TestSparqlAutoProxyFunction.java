package org.aksw.jena_sparql_api.sparql.ext.autoproxy;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlAutoProxyFunction {
    @Test
    public void testAutoProxyFunction01() {
        String str = "SELECT (<java:" + TestSparqlAutoProxyFunction.class.getName() + "#testFunction>('hello') AS ?x) {}";
        String actualValue = MoreQueryExecUtils.INSTANCE.evalQueryToLexicalForm(str);
        Assert.assertEquals("success-hello", actualValue);
    }

    public static String testFunction(String arg) {
        String result = "success-" + arg;
        return result;
        // return Arrays.asList(result);
    }
}
