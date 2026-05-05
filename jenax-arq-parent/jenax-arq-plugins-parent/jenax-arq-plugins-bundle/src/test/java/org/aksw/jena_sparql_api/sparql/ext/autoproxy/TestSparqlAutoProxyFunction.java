package org.aksw.jena_sparql_api.sparql.ext.autoproxy;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSparqlAutoProxyFunction {

    @Test
    public void testAutoProxyFunction01() {
        String actualValue = MoreQueryExecUtils.evalToLexicalForm(QueryExec
            .dataset(DatasetGraphFactory.empty())
            .set(ArqSecurity.symAllowFileAccess, true)
            .query("SELECT (<java:" + TestSparqlAutoProxyFunction.class.getName() + "#testFunction>('hello') AS ?x) {}")
            .build());
        Assertions.assertEquals("success-hello", actualValue);
    }

    @Test
    public void testAutoProxyFunctionSecurity() {
        assertThrows(SecurityException.class, () -> {
            MoreQueryExecUtils.evalToLexicalForm(QueryExec
                .dataset(DatasetGraphFactory.empty())
                .query("SELECT (<java:" + TestSparqlAutoProxyFunction.class.getName() + "#testFunction>('hello') AS ?x) {}")
                .build());
        });
    }

    public static String testFunction(String arg) {
        String result = "success-" + arg;
        return result;
        // return Arrays.asList(result);
    }
}
