package org.aksw.jenax.graphql.sparql.schema;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExec;
import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExecFactory;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlFieldExecImpl;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderGson;
import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformEnrichWithSchema;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformHarmonizeTentris;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaEdge;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNode;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.junit.Test;

import com.google.gson.JsonElement;

import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class TestSparqlGraphQlSchema {
    @Test
    public void test() throws IOException {
        Parser parser = new Parser();
        Document schemaDoc;
        try (Reader reader = new InputStreamReader(
                TestSparqlGraphQlSchema.class.getClassLoader().getResourceAsStream("lingbm.tentris.graphqls"),
                StandardCharsets.UTF_8)) {
            // schema = schemaParser.parse(in);
            schemaDoc = parser.parseDocument(reader);
        }

        schemaDoc = GraphQlUtils.applyTransform(schemaDoc, new TransformHarmonizeTentris());
        GraphQlUtils.println(System.out, schemaDoc);
        // AstPrinter.printAst(document);

        System.out.println(schemaDoc); //GraphQlUtils.toString(schemaDoc));
        System.out.println("========");

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry schema = schemaParser.buildRegistry(schemaDoc);

        SchemaNavigator navigator = SchemaNavigator.of(schema);

        // GraphQlUtils.applyTransform(null, null)

        SchemaNode node = navigator.getOrCreateSchemaNode("Query");
        System.out.println(node.listEdges());

        for (SchemaEdge edge: node.listEdges()) {
            System.out.println(edge.getName());
            System.out.println("------------------------");
            System.out.println(edge.getConnective());
        }


        DatasetGraph dataset = DatasetGraphFactory.empty();

        String queryStr = """
        {
          lecturer(uri: "http://www.Department17.University69.edu/Lecturer3", limit: 1) @pretty {
            doctoralDegreeFrom {
              uri undergraduateDegreeObtainedBystudent {
                uri
                emailAddress
                advisor {
                  uri
                  emailAddress
                  worksFor {
                    uri
                  }
                }
              }
            }
          }
        }
        """;

        Document queryDoc = parser.parseDocument(queryStr);
        queryDoc = GraphQlUtils.applyTransform(queryDoc, new TransformEnrichWithSchema(navigator));

        GraphQlUtils.println(System.out, queryDoc);

        try (GraphQlExec<String> qe = GraphQlExecFactory.of(() -> QueryExec.dataset(dataset), navigator)
                .newBuilder()
                .document(queryDoc)
                .buildForJson()) {

            GraphQlFieldExecImpl impl = (GraphQlFieldExecImpl)qe.getDelegate();
            System.out.println("Query:" + impl.getQuery());

            Iterator<JsonElement> it = impl.asIterator(GraphQlIoBridge.bridgeToJsonInMemory(GonProviderGson.of()));
            while (it.hasNext()) {
                System.out.println(it.next());
                // actual = it.next();
            }
        }

        // Assert.assertEquals(expected, actual);

        // Basic Idea:
        // In the graphql to sparql converter use the schema annotations as a base
        // The main question is: Do annotations on the field override or extend schema annotations?
        // Probably we can have some flag like @override - but I suppose override is the default.
        // I mean: if the field is defined in the schema then why would one override it?
    }
}
