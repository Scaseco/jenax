package org.aksw.jenax.graphql.sparql;

import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlToRonSimple {
    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = TestGraphQlToJsonSimple.createTestDsg();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

    @Test
    public void test05() {
        GraphQlTestUtils.doAssertJsonWithRon(testDsg,
            """
            {
              matches @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s") {
                p2 @one @pattern(of: "SELECT ?x ?z { ?x <http://www.example.org/p2> ?z } ORDER BY ?z LIMIT 1") @rdf(ns: "eg") @prefix(name: "eg", iri: "http://www.example.org/")
              }
            }
            """,
            """
            {
              "matches": [{
                "<http://www.example.org/p2>": "<http://www.example.org/o2>"
              }, {
                "<http://www.example.org/p2>": null
              }]
            }
            """);
    }
}
