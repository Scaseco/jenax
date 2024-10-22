package org.aksw.jena_sparql_api.sparql.ext.fn;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlExtFn {
    @Test
    public void testLineMerge01() {
        String actual = MoreQueryExecUtils.INSTANCE.evalQueryToLexicalForm("""
            PREFIX norse: <https://w3id.org/aksw/norse#>
            SELECT ?msg {
              BIND('Hi' AS ?salutation)
              BIND(norse:sparql.fn.of(?x, CONCAT(?salutation, ' ', ?x)) AS ?helloFn)
              BIND(norse:sparql.fn.call(?helloFn, 'Lorenz') AS ?msg)
            }
        """);
        Assert.assertEquals("Hi Lorenz", actual);
    }
}
