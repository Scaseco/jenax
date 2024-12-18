package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests where fields access variables of ancestors (not just immediate parents). */
public class TestGraphQlVarAccess {

    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = RDFParser.fromString(
                """
                PREFIX : <http://www.example.org/>
                :s1 :p1 :o1 .
                :s1 :p2 :o2 .
                :s2 :p1 :o3 .
                """, Lang.TRIG)
            .toDatasetGraph();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

    @Test
    public void test03() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              Subjects
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")
              {
                style {
                  color @bind(of: "'red'")
                  fillColor @bind(of: "CONCAT('#', SUBSTR(MD5(STR(?s)), 1, 6))")
                  colors @pattern(of: "SELECT * { VALUES ?x { 'red' 'blue' } } ORDER BY DESC(?x)", from: [], to: "x")
                }
              }
            }
            """,
            """
            {
              "http://www.example.org/s1":[{"style":{"color":"red","fillColor":"#363947","colors":["red","blue"]}}],
              "http://www.example.org/s2":[{"style":{"color":"red","fillColor":"#b0e502","colors":["red","blue"]}}]
            }
            """);
    }

    @Test
    public void test04() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              Subjects
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")
              {
                properties {
                  objects @one @pattern(of: "?s <http://www.example.org/p1> ?o", from: "s", to: "o")
                }
              }
            }
            """,
            """
            {
              "http://www.example.org/s1": [{
                "properties": {
                  "objects": "http://www.example.org/o1"
                }
              }],
              "http://www.example.org/s2":[{
                "properties": {
                  "objects":"http://www.example.org/o3"
                }
              }]
            }
            """);
    }

    @Test
    public void test05() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              Subjects
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")
              {
                p1 @one @pattern(of: "?s <http://www.example.org/p1> ?o . FILTER(BOUND(?o))", from: "s", to: "o")
              }
            }
            """,
            """
            {
              "http://www.example.org/s1":[{"p1":"http://www.example.org/o1"}],
              "http://www.example.org/s2":[{"p1":"http://www.example.org/o3"}]
            }
            """);
    }

    @Test
    public void test06() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              Subjects
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")
              {
                properties {
                  p1 @one @pattern(of: "?s <http://www.example.org/p1> ?o . FILTER(BOUND(?o))")
                }
              }
            }
            """,
            """
            {
              "http://www.example.org/s1":[{"properties":{"p1":"http://www.example.org/o1"}}],
              "http://www.example.org/s2":[{"properties":{"p1":"http://www.example.org/o3"}}]
            }
            """);
    }

    @Test
    public void test07() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            query DirectExposure {
              triples(limit: 1) @pattern(of: "SELECT * { ?s ?p ?o } ORDER BY ?s ?p ?o LIMIT 1", to: ["s", "p", "o"]) {
                subject {
                  kind     @bind(of: "IF(isIRI(?s), 'IRI', 'BNODE')")
                  value    @bind(of: "STR(?s)")
                }
                property {
                  kind     @bind(of: "'IRI'")
                  value    @bind(of: "STR(?p)")
                }
                object {
                  kind     @bind(of: "IF(isIRI(?o), 'IRI', IF(isBlank(?o), 'BNODE', 'LITERAL'))")
                  datatype @bind(of: "DATATYPE(?o)") @skipIfNull
                  value    @bind(of: "STR(?o)")
                }
              }
            }
            """,
            """
            {
              "triples":[{
                "subject":{"kind":"IRI","value":"http://www.example.org/s1"},
                "property":{"kind":"IRI","value":"http://www.example.org/p1"},
                "object":{"kind":"IRI","value":"http://www.example.org/o1"}
              }]
            }
            """);
    }


}
