package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlToSparqlMultiVars {
    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = RDFParser.fromString(
                """
                PREFIX : <http://www.example.org/>
                []
                  a :Location ;
                  :city :Leipzig ;
                  :country :Germany .

                :observation1 :city :Leipzig ;
                              :country :Germany ;
                              :measuredTemperature 10 .

                :observation2 :city :Leipzig ;
                              :country :Germany ;
                              :measuredTemperature 20 .
                """, Lang.TRIG)
            .toDatasetGraph();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

    @Test
    public void test01() {
        TestGraphQlUtils.doAssert(testDsg,
            """
            {
              Locations @pattern(of: "SELECT ?city ?country { ?s a :Location ; :city ?city ; :country ?country } ORDER BY ?city ?country", from: ["country", "city"], to: ["country", "city"])
                        @prefix(name: "", iri: "http://www.example.org/")
              {
                avgTemperature @pattern(of: "SELECT ?city ?country (AVG(?temp) AS ?avg) { ?x :city ?city ; :country ?country ; :measuredTemperature ?temp } GROUP BY ?city ?country", from: ["country", "city"], to: "avg")
              }
            }
            """,
            """
            {"Locations":[{"avgTemperature":[15]}]}
            """);
    }

    @Test
    public void test02() {
        TestGraphQlUtils.doAssert(testDsg,
            """
            {
              Locations @pattern(of: "SELECT ?city ?country { ?s a :Location ; :city ?city ; :country ?country } ORDER BY ?city ?country", from: ["country", "city"], to: ["country", "city"])
                        @prefix(name: "", iri: "http://www.example.org/")
              {
                city    @via(of: "city")    @one
                country @via(of: "country") @one
                avgTemperature @pattern(of: "SELECT ?city ?country (AVG(?temp) AS ?avg) { ?x :city ?city ; :country ?country ; :measuredTemperature ?temp } GROUP BY ?city ?country", from: ["country", "city"], to: "avg")
                               @one
              }
            }
            """,
            """
            {"Locations":[{"city":"http://www.example.org/Leipzig","country":"http://www.example.org/Germany","avgTemperature":15}]}
            """);
    }
}
