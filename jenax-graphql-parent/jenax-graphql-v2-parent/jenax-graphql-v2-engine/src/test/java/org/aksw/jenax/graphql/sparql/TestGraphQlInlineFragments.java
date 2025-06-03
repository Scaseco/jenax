package org.aksw.jenax.graphql.sparql;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphQlInlineFragments {
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

        // TODO Setup a schema with the filter annotation on a type and test whether
        //   it is properly added to the fragment.
//
//        Parser parser = new Parser();
//        Document schema = parser.parse("""
//            # The Agent interface: shared fields across AiAgent and Robot
//            interface Agent {
//              id: ID!
//              name: String!
//            }
//
//            # Subtype: AI Agent with a modelVersion field
//            type AiAgent implements Agent @type(iri: "") {
//              id: ID!
//              name: String!
//              modelVersion: String!
//            }
//
//            # Subtype: Robot with a hardwareSpec field
//            type Robot implements Agent {
//              id: ID!
//              name: String!
//              hardwareSpec: String!
//            }
//
//            # Union of all Agents (optional, depending on how you want to query)
//            union AgentUnion = AiAgent | Robot
//
//            # Query type
//            type Query
//              @prefix(name: "", iri: "http://www.example.org/")
//            {
//              # Get a single agent by ID
//              agent(id: ID!): Agent
//
//              # Get all agents (could be mixed types)
//              agents: [Agent!]!
//
//              # Separate queries if needed
//              aiAgents: [AiAgent!]!
//              robots: [Robot!]!
//            }
//        """);
//

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
                        @prefix(name: "", iri: "http://www.example.org/")
              {
                ... on Agents @filter(by: "EXISTS { ?s a :AiAgent }") {
                  id @to
                  label @one @rdf(iri: ":label")
                  modelVersion @one @rdf(iri: ":modelVersion")
                }
              }
            }
            """,
            """
            { "match": {"id": "http://www.example.org/Anne", "label": "Anne", "modelVersion": "omniscius" } }
            """
        );
    }
}
