package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlOnDatasets {
    public static DatasetGraph testDsg;

    @BeforeClass
    public static void tearUp() {
        testDsg = RDFParser.fromString(
                """
                PREFIX : <http://www.example.org/>
                :g1 {
                  :s1 :p1 :o1 .
                  :s1 :p2 :o2 .
                  :s2 :p1 :o3 .
                }
                :g2 {
                  :s1 :p1 :o1 .
                }
                """, Lang.TRIG)
            .toDatasetGraph();
    }

    @Test
    public void test01() {
        GraphQlTestUtils.doAssertJson(testDsg,
            """
            { graphs @pattern(of: "SELECT ?g { GRAPH ?g { } } ORDER BY ?g") }
            """,
            """
            {
              graphs: [
                "http://www.example.org/g1",
                "http://www.example.org/g2"
              ]
            }
            """);
    }

}
