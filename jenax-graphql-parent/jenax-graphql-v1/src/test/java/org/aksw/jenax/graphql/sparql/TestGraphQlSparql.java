package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.aksw.jenax.dataaccess.sparql.polyfill.detector.MainCliSparqlPolyfillModel;
import org.aksw.jenax.graphql.impl.common.GraphQlExecUtils;
import org.aksw.jenax.graphql.impl.common.RdfGraphQlExecUtils;
import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecFactory;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillRewriteJava;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillSuggestionRule;
import org.aksw.jenax.ron.GraphOverRdfObject;
import org.aksw.jenax.ron.RdfElementVisitorRdfToJsonNt;
import org.aksw.jenax.ron.RdfObject;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.util.NodeFactoryExtra;
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
        RDFDataSource dataSource = RDFDataSources.of(ds);
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
        RDFDataSource dataSource = RDFDataSources.of(ds);
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
        RDFDataSource dataSource = RDFDataSources.of(ds);
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

        // JsonElement expected = getResourceAsJson("graphql/test01-result.json", gson);

        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RDFDataSource dataSource = RDFDataSources.of(ds);
        RdfGraphQlExecFactory gef = GraphQlExecFactoryOverSparql.of(dataSource);
        RdfObject actual = RdfGraphQlExecUtils.write(gef.newBuilder().setDocument(queryStr).setJsonMode(false).build());

        // JsonWriter jsonWriter = gson.newJsonWriter(new PrintWriter(System.out));
        // ObjectNotationWriter visitor = new RdfObjectNotationWriterViaJson(gson, jsonWriter);


        // Model model = ModelFactory.createDefaultModel();
        // GraphOverRdfObject.streamTriples(actual).forEach(model.getGraph()::add);
        // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);



        JsonElement json = actual.accept(new RdfElementVisitorRdfToJsonNt());
        System.out.println(gson.toJson(json));

        // System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(actual));

        // Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPolyfill() throws IOException {

        // Set up a local data source with polyfill suggestions
        Model model = ModelFactory.createDefaultModel();
        MainCliSparqlPolyfillModel.initDefaultSuggestions(model);
        RDFDataSource dataSource = RDFDataSources.of(model);

        String queryStr = """
            query($limit: Int!) {
              PolyfillSuggestionRule(limit: $limit) @debug @many(cascade: true)
                 @sparql(inject: "SELECT ?s { ?s polyfill:suggestion ?o }")
                 # @class
                 @rdf (
                   prefixes: {
                     rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                     rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                     polyfill: "https://w3id.org/aksw/norse#polyfill."
                   },
                   base: polyfill
                 )
              {
                type @rdf(ns: rdf)
                label @rdf(ns: rdfs)
                comment(orderBy: { comment: ASC }) @rdf(ns: rdfs)
                suggestion {
                ... on PolyfillRewriteJava @rdf(ns: "java://org.aksw.jenax.model.polyfill.domain.api.") @class {
                    type @rdf(ns: rdf)
                    label @rdf(ns: rdfs)
                    javaClass @rdf(ns: polyfill) @one # BUG: for some reason the fragement namespace is used here
                  }
                }
              }
            }
            """;

        // Create a GraphQL engine over the RDF data source.
        RdfGraphQlExecFactory gef = GraphQlExecFactoryOverSparql.of(dataSource);

        // Get the JSON result of the GraphQL query
        // -------------------------------------------------------------------

        GraphQlExec geJson = gef.toJson().newBuilder()
                .setDocument(queryStr)
                .setVar("limit", IntValue.of(1))
                .build();

        // System.out.println(gson.toJson(gson.fromJson(geJson.getDataProviders().get(0).getMetadata().get("sparqlQuery"), JsonElement.class)));
        // System.out.println(geJson.getDataProviders().get(0).getMetadata().get("sparqlQuery").toString().replace("\\n", "\n"));

        // Print out the JSON using GSON API
        // System.out.println(gson.toJson(GraphQlExecUtils.materialize(geJson)));


        // Get the RDF Object result of the GraphQL query
        // -------------------------------------------------------------------

        RdfGraphQlExec ge = gef.newBuilder()
                .setDocument(queryStr)
                .setVar("limit", NodeFactoryExtra.intToNode(1))
                .setJsonMode(false)
                .build();

        // Collect the stream of RDF objects into a list
        List<RdfObject> objs = ge.getDataProviders().get(0).openStream()
            .map(item -> item.getAsObject())
            .toList();

        // Print out the RDF objects using JSON where all literals
        // are RDF terms in N-Triples syntax
        // Keys may start with '^' to indicate reverse properties
        for (RdfObject obj : objs) {
            JsonElement json = obj.accept(new RdfElementVisitorRdfToJsonNt());
            System.out.println(gson.toJson(json));
        }



        // Access the RDF object with a Java domain model
        for (RdfObject obj : objs) {
            PolyfillSuggestionRule rule = obj.as(PolyfillSuggestionRule.class);
            System.out.println(rule.getLabel());
            System.out.println(rule.getComment());
            Resource suggest = rule.getSuggestion();
            if (suggest instanceof PolyfillRewriteJava prj) {
                System.out.println("  Java class: " + prj.getJavaClass());
            } else {
                System.out.println("  Unsupported polyfill rewrite");
            }
        }

        // Extract the triples from the RDF object
        // This is akin to extracting triples from JSON-LD
        Model outModel = ModelFactory.createDefaultModel();
        for (RdfObject obj : objs) {
            GraphOverRdfObject.streamTriples(obj).forEach(outModel.getGraph()::add);
        }

        // Prettily print out the extracted triples
        RDFDataMgr.write(System.err, outModel, RDFFormat.TURTLE_PRETTY);
    }


    @Test
    public void testPatternTraversals_01() {
        String queryStr = """
            {
              @pattern(of: "DISTINCT ?s { ?s ?p ?o }") @bind(var: 'x', expr: '?y') @index(by: "x", oneIf: "true")
              root
                id @ref(var: s) @one
                p @pattern(of: "?s ?p ?o", src:"s" tgt: "p")
                o @pattern(of: "?s ?p ?o", src:"s" tgt: "p")
            }
            """;

    }



//  System.err.println(ge.getDataProviders().iterator().next().getQuery());
//  RdfObject actual = RdfGraphQlExecUtils.write(ge);

  // JsonWriter jsonWriter = gson.newJsonWriter(new PrintWriter(System.out));
  // ObjectNotationWriter visitor = new RdfObjectNotationWriterViaJson(gson, jsonWriter);




//  JsonElement json = arr.accept(new RdfElementVisitorRdfToJsonNt());
//  System.out.println(gson.toJson(json));

  // System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(actual));

  // Assert.assertEquals(expected, actual);
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
