package org.aksw.jena_sparql_api.sparql.ext.lambda;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSparqlExtLambda {
    @Test
    public void testLineMerge01() {
        String actual = MoreQueryExecUtils.INSTANCE.evalQueryToLexicalForm("""
            PREFIX norse: <https://w3id.org/aksw/norse#>
            SELECT ?msg {
              BIND('Hi' AS ?salutation)
              BIND(norse:lambda.of(?x, CONCAT(?salutation, ' ', ?x)) AS ?helloFn)
              BIND(norse:lambda.call(?helloFn, 'Lorenz') AS ?msg)
            }
        """);
        Assertions.assertEquals("Hi Lorenz", actual);
    }
}
