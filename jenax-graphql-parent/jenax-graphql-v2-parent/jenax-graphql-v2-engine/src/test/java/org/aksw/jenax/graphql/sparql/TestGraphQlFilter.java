package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlFilter {
    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = RDFParser.fromString(
            """
            PREFIX : <http://www.example.org/>
            :Anne a :AiAgent ; :label "Anne" ; :modelVersion "omniscius" .

            :Bob a :Robot ; :label "Bob" ; :hardwareSpec "quantum" .
            """, Lang.TRIG)
        .toDatasetGraph();
    }

    @AfterClass
    public static void tearDown() {
        testDsg = null;
    }

    @Test
    public void test01() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              match @one @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o }", from: "s", to: "s")
                         @filter(by: "contains(lcase(str(?s)), lcase('bob'))")
                         @prefix(name: "", iri: "http://www.example.org/")
              {
                id @to
                label @one @rdf(iri: ":label")
              }
            }
            """,
            """
            { "match": {"id": "http://www.example.org/Bob", "label": "Bob" } }
            """
        );
    }

    @Test
    public void test02() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              match @one @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o }", from: "s", to: "s")
                         @filter(by: "exists { ?s :modelVersion 'omniscius' }")
                         @prefix(name: "", iri: "http://www.example.org/")
              {
                id @to
                label @one @rdf(iri: ":label")
              }
            }
            """,
            """
            { "match": {"id": "http://www.example.org/Anne", "label": "Anne" } }
            """
        );
    }

    @Test
    public void test03() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            {
              match(uri: "http://www.example.org/Anne")
                         @one @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o }", from: "s", to: "s")
                         @filter(when: "?uri != ''", by: "?s = IRI(?uri)")
                         @prefix(name: "", iri: "http://www.example.org/")
              {
                id @to
                label @one @rdf(iri: ":label")
              }
            }
            """,
            """
            { "match": {"id": "http://www.example.org/Anne", "label": "Anne" } }
            """
        );
    }
}
