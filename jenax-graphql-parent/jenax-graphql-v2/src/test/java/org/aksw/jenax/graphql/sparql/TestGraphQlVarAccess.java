package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlVarAccess {

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

    @Test
    public void test03() {
        GraphQlTestUtils.doAssert(testDsg,
            """
            {
              Subjects
                @pattern(of: "SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s", from: "s", to: "s") @index(by: "?s")
              {
                style {
                  color @bind(of: "'red'")
                  fillColor @bind(of: "CONCAT('#', SUBSTR(MD5(STR(?s)), 1, 6))")
                  colors @pattern(of: "VALUES ?x { 'red' 'blue' }", from: [], to: "x")
                }
              }
            }
            """,
            """
            {
              "http://www.example.org/s1":[{"style":[{"color":"red","fillColor":"#363947","colors":["red","blue"]}]}],
              "http://www.example.org/s2":[{"style":[{"color":"red","fillColor":"#b0e502","colors":["red","blue"]}]}]
            }
            """);
    }

}
