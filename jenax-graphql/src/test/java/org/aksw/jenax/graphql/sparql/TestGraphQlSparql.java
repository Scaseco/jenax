package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.graphql.impl.common.GraphQlExecUtils;
import org.aksw.jenax.graphql.impl.common.RdfGraphQlExecUtils;
import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecFactory;
import org.aksw.jenax.io.rdf.json.RdfElementVisitorRdfToJsonNt;
import org.aksw.jenax.io.rdf.json.RdfObject;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import graphql.language.Document;
import graphql.language.IntValue;
import graphql.language.Value;
import graphql.parser.Parser;

public class TestGraphQlSparql {
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping() // If enabled then e.g. the angular brackets in <my:iri> get escaped.
            .setPrettyPrinting()
            .create();

    protected static RdfGraphQlExecFactory gef;

    @BeforeClass
    public static void beforeClass() {
        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RdfDataSource dataSource = RdfDataEngines.of(ds);
        gef = GraphQlExecFactoryOverSparql.of(dataSource); //  .autoConfEager
    }

    @AfterClass
    public static void afterClass() {
        // gef.close();
    }

    @Test
    public void testPokemon01() {
        String queryStr = getResourceAsString("graphql/test01-query.graphql");
        JsonElement expected = getResourceAsJson("graphql/test01-result.json", gson);

        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RdfDataSource dataSource = RdfDataEngines.of(ds);
        RdfGraphQlExecFactory gef = GraphQlExecFactoryOverSparql.autoConfEager(dataSource);
        JsonObject actual = GraphQlExecUtils.materialize(gef.toJson(), queryStr);

        // System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(actual));

        Assert.assertEquals(expected, actual);
    }

    // @Test
    public void testInlineFragments02() {

        // System.out.println(OpVars.fixedVars(Algebra.compile(QueryFactory.create("SELECT * { ?s a ?t . OPTIONAL { ?s a ?x } FILTER(?x = ?s) }"))));

        String queryStr = """
            {
              user {
                ... on User @test {
                  id
                  username
                }
              }
            }
            """;
        Parser parser = new Parser();
        Document doc = parser.parseDocument(queryStr);
        // System.out.println(queryStr);
        // JsonElement expected = getResourceAsJson("graphql/test01-result.json", gson);

        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RdfDataSource dataSource = RdfDataEngines.of(ds);
        RdfGraphQlExecFactory gef = GraphQlExecFactoryOverSparql.of(dataSource);

        JsonObject actual = GraphQlExecUtils.materialize(gef.toJson(), queryStr);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(actual));

        // Assert.assertEquals(expected, actual);
    }

    // @Test
    public void testInlineFragments03() {
        // System.out.println(OpVars.fixedVars(Algebra.compile(QueryFactory.create("SELECT * { ?s a ?t . OPTIONAL { ?s a ?x } FILTER(?x = ?s) }"))));

        String queryStr
                = "{\n"
                + "  user {\n"
                + "    ... on User @test {\n"
                + "      id\n"
                + "      username\n"
                + "      ... on Agent {\n"
                + "        ... { test }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "";

        Parser parser = new Parser();
        Document doc = parser.parseDocument(queryStr);
        System.out.println(doc);
    }


    @Test
    public void testVariables_01() {

        String queryStr = """
            query($limit: Int!) {
              Pokemon(limit: $limit, orderBy: { label: ASC }) @class
                 @debug
                 @rdf (
                   prefixes: {
                     rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                     pokedex: "http://pokedex.dataincubator.org/pkm/"
                   },
                   base: pokedex
                 )
              {
                xid,
                label @rdf(ns: rdfs) @one
              }
            }
            """;

        String expected = """
            { "Pokemon": [
              {
                "label": "Electrode"
              }
            ] }
        """;

        evaluate(expected, queryStr, Map.of("limit", IntValue.of(1)));
    }
    // System.out.println(OpVars.fixedVars(Algebra.compile(QueryFactory.create("SELECT * { ?s a ?t . OPTIONAL { ?s a ?x } FILTER(?x = ?s) }"))));

    @Test
    public void testFragments04() {

        String queryStr = """
            {
              Pokemon(orderBy: { colour: ASC }, limit: 1) @class
               @debug
                @rdf (
                  prefixes: {
                    rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                    pokedex: "http://pokedex.dataincubator.org/pkm/"
                  },
                  base: pokedex
                )
              {
                label @rdf(ns: rdfs) @one,
                colour @one,
                speciesOf @inverse @one {
                  label @rdf(ns: rdfs) @one
                }

                ... on Foo @sparql(fragment: "SELECT ?x { ?x pokedex:colour 'black' }")  {
                  sameAsLinks @rdf(iri: "http://www.w3.org/2002/07/owl#sameAs")
                }

                ... on Bar @sparql(fragment: "SELECT ?x { ?x pokedex:colour 'black' }") {
                  sameAsLinks(orderBy: {link: ASC }) @as(name: link) @rdf(iri: "http://www.w3.org/2002/07/owl#sameAs")
                }
              }
            }
            """;

        String expected = """
            { "Pokemon": [
              {
                "label": "Umbreon",
                "colour": "black",
                "speciesOf": {
                  "label": "Moonlight Pokémon"
                },
                "sameAsLinks": [
                  "http://rdf.freebase.com/ns/guid.9202a8c04000641f8000000000260f5f",
                  "http://pokedex.dataincubator.org/pokemon/umbreon",
                  "http://dbpedia.org/resource/Umbreon"
                ],
                "sameAsLinks": [
                  "http://dbpedia.org/resource/Umbreon",
                  "http://pokedex.dataincubator.org/pokemon/umbreon",
                  "http://rdf.freebase.com/ns/guid.9202a8c04000641f8000000000260f5f"
                ]
              }
            ] }
        """;

        evaluate(expected, queryStr, null);
    }


    // @Test
    public void testFragments04b() {

        // Issue: Order by with a field defined in a fragment does not work - { orderBy: { fragmentField: ASC } }
        String queryStr = """
            fragment pokemonFields on Thing {
              label @rdf(ns: rdfs) @one,
              colour @one,
              speciesOf @inverse @one {
                label @rdf(ns: rdfs) @one
              }
            }

            query {
              Pokemon(limit: 1, orderBy: { colour2: ASC }) @class
               @debug
                @rdf (
                  prefixes: {
                    rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                    pokedex: "http://pokedex.dataincubator.org/pkm/"
                  },
                  base: pokedex
                )
              {
                ... pokemonFields
                colour2:colour @one
              }
            }
            """;
        // Issue: why does colour2 fail to resolve?

        String expected = """
            { "Pokemon": [
              {
                "label": "Umbreon",
                "colour": "black",
                "speciesOf": {
                  "label": "Moonlight Pokémon"
                },
                "colour2": "black"
              }
            ] }
        """;

        evaluate(expected, queryStr, null);
    }

    @Test
    public void testPokemon02() throws IOException {

        String queryStr = """
            query($limit: Int!) {
              Pokemon(limit: $limit, orderBy: { l1: ASC }) @class
                 @debug
                 @rdf (
                   prefixes: {
                     rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                     pokedex: "http://pokedex.dataincubator.org/pkm/"
                   },
                   base: pokedex
                 )
              {
                # xid,
                label @as(name: l1) @rdf(ns: rdfs) @one
                speciesOf @inverse @many {
                  label @rdf(ns: rdfs) @one
                }
              }
            }
            """;

        // Actually, we want to write GraphQL over SPARQL leveraging the usual SPARQL notations:
        // The problem is, that the colon syntax is already reserved for field aliases...
        String queryStr2 = """
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX pokedex: <http://pokedex.dataincubator.org/pkm/>
                PREFX : <http://pokedex.dataincubator.org/pkm/>

                query($limit: xsd:numeric!) {
                  :Pokemon(limit: $limit, orderBy: { l1: ASC }) @class @debug
                  {
                    rdfs:label @as(name: l1) @one
                    :speciesOf @inverse @many {
                      rdfs:label @rdf(ns: rdfs) @one
                    }
                  }
                }
                """;

        // JsonElement expected = getResourceAsJson("graphql/test01-result.json", gson);

        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RdfDataSource dataSource = RdfDataEngines.of(ds);
        RdfGraphQlExecFactory gef = GraphQlExecFactoryOverSparql.of(dataSource);
        RdfObject actual = RdfGraphQlExecUtils.write(gef.newBuilder().setDocument(queryStr).setJsonMode(false).build());

        // JsonWriter jsonWriter = gson.newJsonWriter(new PrintWriter(System.out));
        // ObjectNotationWriter visitor = new RdfObjectNotationWriterViaJson(gson, jsonWriter);


        JsonElement json = actual.accept(new RdfElementVisitorRdfToJsonNt());
        System.out.println(gson.toJson(json));

        // System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(actual));

        // Assert.assertEquals(expected, actual);
    }


    public void evaluate(String expectedJson, String documentString, Map<String, Value<?>> assignments) {
        JsonElement expectedData = gson.fromJson(expectedJson, JsonElement.class);

        GraphQlExec qe = gef.toJson().create(documentString, assignments);
        JsonObject actualResponse = GraphQlExecUtils.materialize(qe);
        JsonElement actualData = actualResponse.get("data");

        boolean isEquals = Objects.equals(expectedData, actualData);
        if (!isEquals) {
            System.err.println(gson.toJson(actualResponse));
        }

        Assert.assertEquals(expectedData, actualData);
    }

    private static JsonElement getResourceAsJson(String name, Gson gson) {
        JsonElement result = gson.fromJson(getResourceAsString(name), JsonElement.class);
        return result;
    }

    private static String getResourceAsString(String name) {
        String result;
        try {
            result = Resources.toString(
                    Resources.getResource(name), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
