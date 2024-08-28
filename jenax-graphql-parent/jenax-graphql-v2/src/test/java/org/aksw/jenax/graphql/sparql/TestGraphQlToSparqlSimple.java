package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.lang.ParserARQ;
import org.apache.jena.sparql.lang.sparql_12.SPARQLParser12;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlToSparqlSimple {
    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = RDFParser.fromString(
                """
                PREFIX : <http://www.example.org/>
                :s1 :p1 :o1 .
                :s1 :p2 :o2 .
                :s2 :p1 :o1 .
                """, Lang.TRIG)
            .toDatasetGraph();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

//    @Test
//    public void vars() {
//        // dummy test, move elsewhere
//        // System.out.println(OpVars.mentionedVars(Algebra.compile(ParserARQ.parseElement("{FILTER(?x)}"))));
//
//        // ?x ?a ?z - no ?y
//        System.out.println(OpVars.mentionedVars(Algebra.compile(ParserARQ.parseElement("{BIND(?x AS ?y) { SELECT ?z { FILTER(?a)}}}"))));
//
//    }

    @Test
    public void test01() {
        GraphQlTestUtils.doAssert(testDsg,
            """
            { Subjects @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") }
            """,
            """
            {"Subjects":["http://www.example.org/s1", "http://www.example.org/s2"]}
            """);
    }

    @Test
    public void test02() {
        GraphQlTestUtils.doAssert(testDsg,
            """
            { Triples @pattern(of: "?s ?p ?o", from: "s", to: "o") @index(by: "?p") }
            """,
            """
            {"http://www.example.org/p1":["http://www.example.org/o1","http://www.example.org/o1"],"http://www.example.org/p2":["http://www.example.org/o2"]}
            """);
    }

    @Test
    public void test03() {
        GraphQlTestUtils.doAssert(testDsg,
            """
            { Subjects @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")}
            """,
            """
            {"http://www.example.org/s1":["http://www.example.org/s1"],"http://www.example.org/s2":["http://www.example.org/s2"]}
            """);
    }

    @Test
    public void test04() {
        // Note the query itself does not join the xyz pattern correctly with the count of of the properties in a meaningful way,
        // however, it revealed a bug in AccStateMap:
        // When ifOne: "true" was true but multiple values for a key were encountered, the excessive values were not ignored.
        // Instead, it resulted in exceptions about illegally nested JSON.
        GraphQlTestUtils.doAssert(testDsg,
            """
            {
              matches(limit: 2)
                  @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o }", from: "s", to: "s")
                  @prefix(name: "afn", iri: "http://jena.apache.org/ARQ/function#")
              {
                properties @pattern(of: "?x ?y ?z . { SELECT ?y (COUNT(DISTINCT(?z)) AS ?c) { ?x ?y ?z } GROUP BY ?x ?y}", from: "x", to: ["z", "c"]) @index(by: "afn:localname(?y)", oneIf: "?c <= 1")
              }
            }
            """,
            """
            {"matches":[{"p1":"<http://www.example.org/o1> 1"},{"p1":"<http://www.example.org/o1> 1","p2":"<http://www.example.org/o2> 1"}]}
            """);
    }

    @Test
    public void test05() {
        GraphQlTestUtils.doAssert(testDsg,
            """
            {
              matches @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s") {
                p2 @one @pattern(of: "SELECT ?x ?z { ?x <http://www.example.org/p2> ?z } ORDER BY ?z LIMIT 1")
              }
            }
            """,
            """
            {
              "matches": [{
                "p2": "http://www.example.org/o2"
              }, {
                "p2": null
              }]
            }
            """);
    }

}
