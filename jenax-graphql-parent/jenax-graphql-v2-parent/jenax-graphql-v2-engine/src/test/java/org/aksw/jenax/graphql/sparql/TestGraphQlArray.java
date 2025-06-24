package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestGraphQlArray {

    public static DatasetGraph testDsg;

    public static DatasetGraph createTestData() {
        DatasetGraph result = RDFParser.fromString(
                """
                PREFIX : <http://www.example.org/>
                :Anne a :Person .
                :Anne :knowsAndroid  :r2d2 .
                :Anne :knowsAndroid :3cpo .
                :Anne :knowsPerson  :Bob .
                :Bob a :Person .
                """, Lang.TRIG)
            .toDatasetGraph();
        return result;
    }

    @BeforeClass
    public static void tearUp() {
        testDsg = createTestData();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

    @Test
    public void testArray01a() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              People
                @prefix(name: "", iri: "http://www.example.org/")
                @pattern(of: "BIND(:Anne AS ?s)")
                @index(by: "?s")
                @array
              {
                androids @pattern(of: "SELECT ?s ?o { ?s :knowsAndroid ?o } ORDER BY ?o") @many
                people   @rdf(iri: ":knowsPerson") @one
              }
            }
            """,
            """
            { "http://www.example.org/Anne": [ "http://www.example.org/3cpo", "http://www.example.org/r2d2", "http://www.example.org/Bob" ] }
            """);
    }

    /**
     * FIXME @one @array does not create a sub array - probably it should (@array should always create an array).
     *
     * The &commat;array annotation makes all child-fields produce array items rather then
     * object members.
     */
    @Test
    public void testArray01b() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              People
                @prefix(name: "", iri: "http://www.example.org/")
                @pattern(of: "BIND(:Anne AS ?s)")
                @index(by: "?s")
                @array
              {
                androids @pattern(of: "SELECT ?s ?o { ?s :knowsAndroid ?o } ORDER BY ?o") @many @array
                people   @rdf(iri: ":knowsPerson") @many @array {
                  foo @to
                }
              }
            }
            """,
            """
            { "http://www.example.org/Anne": [ ["http://www.example.org/3cpo", "http://www.example.org/r2d2" ], [ "http://www.example.org/Bob" ] ] }
            """);
    }
    @Test
    public void testArray02() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              People
                @prefix(name: "", iri: "http://www.example.org/")
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s")
                # @filter(by: "?s = :Anne")
                @index(by: "?s")
                @array
              {
                androids @pattern(of: "SELECT ?s ?o { ?s :knowsAndroid ?o } ORDER BY ?o") @many
                people  @rdf(iri: ":knowsPerson") @one @skipIfNull
              }
            }
            """,
            """
            {
              "http://www.example.org/Anne": [ "http://www.example.org/3cpo", "http://www.example.org/r2d2", "http://www.example.org/Bob" ],
              "http://www.example.org/Bob": []
            }
            """);
    }

    /**
     * FIXME Clarify semantics.
     *   How to produce a top-level array?
     *   Should &commat;array on query level causes the query to return an array?
     */
    @Test
    @Ignore
    public void testArray03() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            query @array {
              Subjects
                @prefix(name: "", iri: "http://www.example.org/")
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s")
              {
                p1 @rdf(iri: ":p1") @one
                p2 @rdf(iri: ":p2") @many
              }
            }
            """,
            """
            [ "http://www.example.org/o1", "http://www.example.org/o2", "http://www.example.org/o3" ]
            """);
    }
}
