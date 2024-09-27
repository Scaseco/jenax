package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.util.Iterator;

import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlFieldExec;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.RdfGraphQlProcessorFactoryImpl;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderApi;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderGson;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderJava;
import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterMapper;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterMapperImpl;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterViaGon;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformAssignGlobalIds;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformExpandShorthands;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformHarmonizePrefixes;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.aksw.jenax.ron.RdfElementVisitorRdfToJsonNt;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.path.P_Path0;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import graphql.language.AstComparator;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.NodeVisitorStub;
import graphql.parser.Parser;

//class TestVisitor extends NodeVisitorStub {
//    @Override
//    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
//        Field newField = field.transform(builder -> {
//            builder.name("yay")
//            .selectionSet(SelectionSet.newSelectionSet().build());
//        });
//
//        context.setVar(String.class, "Hello");
//
//
//        System.out.println("at " +field.getName() + ":" + context.getVarFromParents(String.class));
//        // context.deleteNode();
//        // System.out.println("Status for " + context.thisNode());
////        System.out.println(context.isChanged());
////        System.out.println(context.isDeleted());
//        //return TreeTransformerUtil.changeNode(context, newField);
//        return super.visitField(newField, context);
//
//
//        // context.changeNode(newField);
////        System.out.println("Visited field: " + node.getName());
////        System.out.println("Context value: " + context.getVarFromParents(String.class));
//        // context.setVar(String.class, "Foo");
//        // return super.visitField(newField, context);
//    }
//}

public class TestGraphQlTransform {
    @Test
    public void test_harmonizePrefixes() {
        String inputStr = """
            {
                field
                  @foo
                  @prefix(name: "eg", iri: "http://www.example.org/")
                  @bar
                  @prefix(name: "rdf", iri: "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                  @bar
                {
                  field2 @foo @prefix(name: "eg", iri: "http://ex.org/") @bar
                }
            }
            """;

        String expectedStr = """
            {
              field @prefix(map: {eg : "http://www.example.org/", rdf : "http://www.w3.org/1999/02/22-rdf-syntax-ns#"}) @foo @bar @bar {
                field2 @prefix(map: {eg : "http://ex.org/"}) @foo @bar
              }
            }
            """;

        assertTransformResult(inputStr, expectedStr, new TransformHarmonizePrefixes());
    }

    // @Test
    public void test_assignGlobalIds() {
        String inputStr = """
            {
                field {
                  field2 @foo
                }
            }
            """;

        String expectedStr = """
            {
              field @globalId(id: "field_0") {
                field2 @globalId(id: "field_1") @foo
              }
            }
            """;

        assertTransformResult(inputStr, expectedStr, TransformAssignGlobalIds.of("field_", 0));
    }

    @Test
    public void test_expandShortcuts() {
        String inputStr = """
            {
                knows @rdf(ns: "foaf") @prefix(name: "foaf", iri: "http://xmlns.com/foaf/0.1/"){
                  firstName @rdf(iri: "foaf:firstName") @reverse
                }
            }
            """;

        String expectedStr = """
            {
              knows @emitRdfKey(iri: "http://xmlns.com/foaf/0.1/knows") @pattern(of: "?s <http://xmlns.com/foaf/0.1/knows> ?o", from: "s", to: "o") @prefix(name: "foaf", iri: "http://xmlns.com/foaf/0.1/") {
                firstName @emitRdfKey(iri: "http://xmlns.com/foaf/0.1/firstName", reverse: true) @pattern(of: "?s <http://xmlns.com/foaf/0.1/firstName> ?o", from: "o", to: "s")
              }
            }
            """;

        assertTransformResult(inputStr, expectedStr, new TransformExpandShorthands());
    }

    @Test
    public void test_convertToSparql() {
        String inputStr = """
            {
              Dataset(limit: 10) @pattern(of: "?s a dcat:Dataset", to: "s") @prefix(name: "dcat", iri: "http://www.w3.org/ns/dcat#") @graph {
                distribution @rdf(ns: "dcat") {
                  downloadURL @rdf(ns: "dcat")
                }
              }
            }
            """;

        String expectedStr = """
            {foo}
            """;

        // execOnService("https://maven.aksw.org/sparql", inputStr);
    }


    // @Test // TODO Sever remote service dependency
    public void test_convertToSparql2() throws IOException {
        // Fields have different "aspects":
        // The source variables that connect to the parent
        // The target variables to which immediate children connect to by default
        // The output variables from which the output is generated. Defaults to the target.
        // Source and target are initially derived from the 'connective' specified by pattern, but can be overidden.

        String inputStr = """
            {
              Establishment:foo(limit: 1, offset: 11) @one
                  @pattern(of: "?s a :Establishment", from: "s", to: "s")
                  @graph(iri: "https://data.coypu.org/companies/prtr/")
                  @prefix(name: "", iri: "https://schema.coypu.org/global#")
#                {
#                  iri
#                      @one
#                       @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y") @emitRdfKey(iri: "urn:id")
#                }

              Establishment(limit: 10)
                  @pattern(of: "?s a :Establishment", from: "s", to: "s")
#                  @pattern(of: "?s a :Establishment FILTER(?s = <https://data.coypu.org/company/ptrt/01-10-51011008130>)", from: "s", to: "s")
                  @graph(iri: "https://data.coypu.org/companies/prtr/")
                  @prefix(name: "", iri: "https://schema.coypu.org/global#")
                  @prefix(name: "rdfs", iri: "http://www.w3.org/2000/01/rdf-schema#")
                  @prefix(name: "geo", iri: "http://www.opengis.net/ont/geosparql#")
                  @prefix(name: "geof", iri: "http://www.opengis.net/def/function/geosparql/")
                  @prefix(name: "norse", iri: "https://w3id.org/aksw/norse#")
               {
                 iri
                     @one
                     @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y") @emitRdfKey(iri: "urn:id")
                 label @one
                     @rdf(iri: "rdfs:label")
                 {
                   str @one
                       @pattern(of: "BIND(STR(?x) AS ?y)", from: "x", to: "y") @emitRdfKey(iri: "urn:str")
                   lang @one
                       @pattern(of: "BIND(LANG(?x) AS ?y)", from: "x", to: "y") @emitRdfKey(iri: "urn:lang")
                 }
                 location
                     @one
                     @pattern(of: "?s geo:hasGeometry/geo:asWKT ?x BIND(STRDT(STR(geof:asGeoJSON(?x)), norse:json) AS ?o)", from: "s", to: "o")
                     @emitRdfKey(iri: "urn:location")
               }
             }
            """;

//        inputStr = """
//            {
//              Establishment:foo(limit: 1, offset: 11) @one
//                  @pattern(of: "?s a :Establishment", from: "s", to: "s")
//                  @graph(iri: "https://data.coypu.org/companies/prtr/")
//                  @prefix(name: "", iri: "https://schema.coypu.org/global#")
//            }
//            """;

        inputStr = """
            {
              Establishment:foo(limit: 1, offset: 11) @one
                  @pattern(of: "SELECT DISTINCT ?s { ?s a :Establishment }", from: "s", to: "s")
                  @graph(iri: "https://data.coypu.org/companies/prtr/")
                  @prefix(name: "", iri: "https://schema.coypu.org/global#")
              {
                p @pattern(of: "?s ?p ?o", from: "s", to: "o") @index(by: "p")
              }
            }
            """;

        inputStr = """
                {
                  Establishment:foo(limit: 2, offset: 11) @one
                      @pattern(of: "SELECT DISTINCT ?s { ?s a :Establishment }", from: "s", to: "s")
                      @graph(iri: "https://data.coypu.org/companies/prtr/")
                      @prefix(name: "", iri: "https://schema.coypu.org/global#")
                  {
                    p @pattern(of: "?s ?p ?o", from: "s", to: "o") @index(by: "?p")
                  }
                }
                """;

        inputStr = """
                {
                  Establishment:foo(limit: 2, offset: 11)
                      @pattern(of: "SELECT DISTINCT ?s { ?s a :Establishment }", from: "s", to: "s")
                      @graph(iri: "https://data.coypu.org/companies/prtr/")
                      @prefix(name: "", iri: "https://schema.coypu.org/global#")
                      @prefix(name: "afn", iri: "http://jena.apache.org/ARQ/function#")
                  {
                    p @pattern(of: "?s ?p ?o", from: "s", to: "o") @index(by: "afn:localname(?p)", oneIf: "true")
                  }
                }
                """;

        ObjectNotationWriterViaGon<?, ?, ?> backend;
        ObjectNotationWriter<P_Path0, Node> writer;

        if (false) {
            ObjectNotationWriterViaGon<Object, P_Path0, Node> tmp = ObjectNotationWriterViaGon.of(GonProviderJava.newInstance());
            // Plain java
            writer = tmp;
        } else {
            GonProviderApi<JsonElement, String, JsonPrimitive> gsonProvider = GonProviderGson.of();
            ObjectNotationWriterViaGon<JsonElement, String, JsonPrimitive> delegate = ObjectNotationWriterViaGon.of(gsonProvider);
            backend = delegate;
            ObjectNotationWriterMapper<P_Path0, String, Node, JsonPrimitive> tmp =
                    new ObjectNotationWriterMapperImpl<>(delegate, gsonProvider, RdfElementVisitorRdfToJsonNt::path0ToName, RdfElementVisitorRdfToJsonNt::nodeToJsonElement);
            writer = tmp;
        }

        try (GraphQlFieldExec<P_Path0> qe = RdfGraphQlProcessorFactoryImpl.forRon().newBuilder()
                    .document(inputStr)
                    // set mode?
                .build() // or have buildForJson and buildForRdf here?
                // .getFieldProcessor(1).newExecBuilder()
                .newExecBuilder()
                .service(() -> QueryExec.service("http://localhost:8642/sparql"))
                .build()) {


            if (true) {
                Iterator<JsonElement> it = qe.asIterator(GraphQlIoBridge.bridgeRonToJsonInMemory(GonProviderGson.of()));
                while (it.hasNext()) {
                    System.out.println("ITEM: " + it.next());
                }

            } else {
            int i = 0;
            while (qe.sendNextItemToWriter(writer)) {
                System.out.println("Result #" + (++i) + ":");

                Object product = backend.getProduct();
                if (product instanceof JsonElement je) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    System.out.println(gson.toJson(je));
                } else {
                    System.out.println(product);
                }
            }
            writer.flush();
            }
        }

        // # @vocab(iri: "https://schema.coypu.org/global#")

        String expectedStr = """
            {foo}
            """;

        // execOnService("http://localhost:8642/sparql", inputStr);
    }




    // @Test TODO Sever remote service dependency
    public void test_inlineFragment_01() throws IOException {
        // Fields have different "aspects":
        // The source variables that connect to the parent
        // The target variables to which immediate children connect to by default
        // The output variables from which the output is generated. Defaults to the target.
        // Source and target are initially derived from the 'connective' specified by pattern, but can be overidden.

        String inputStr = """
            {
              Establishment(limit: 10)
                  @pattern(of: "?s a :Establishment", from: "s", to: "s")
                  @graph(iri: "https://data.coypu.org/companies/prtr/")
                  @prefix(name: "", iri: "https://schema.coypu.org/global#")
                  @prefix(name: "rdfs", iri: "http://www.w3.org/2000/01/rdf-schema#")
                  @prefix(name: "geo", iri: "http://www.opengis.net/ont/geosparql#")
                  @prefix(name: "geof", iri: "http://www.opengis.net/def/function/geosparql/")
                  @prefix(name: "norse", iri: "https://w3id.org/aksw/norse#")
               {
                 iri
                     @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y")
                     @emitRdfKey(iri: "urn:id") @one
                 ... on Foobar @filter(if: "exists { ?s a :Establishment }") {
                   location
                       @one
                       @pattern(of: "?s geo:hasGeometry/geo:asWKT ?x BIND(STRDT(STR(geof:asGeoJSON(?x)), norse:json) AS ?o)", from: "s", to: "o")
                       @emitRdfKey(iri: "urn:location")
                   {
                     loc @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y")
                     label
                         @one @rdf(iri: "rdfs:label")
                   }
                 }
               }
             }
            """;

        try (GraphQlFieldExec<String> qe = RdfGraphQlProcessorFactoryImpl.forJson().newBuilder()
                    .document(inputStr)
                    // set mode?
                .build() // or have buildForJson and buildForRdf here?
                // .getFieldProcessor(1).newExecBuilder()
                .newExecBuilder()
                .service(() -> QueryExec.service("http://localhost:8642/sparql"))
                .build()) {

            // GraphQlExecUtils.write(System.out, qe);

            Iterator<JsonElement> it = qe.asIterator(GraphQlIoBridge.bridgeToJsonInMemory(GonProviderGson.of()));
            while (it.hasNext()) {
                System.out.println("ITEM: " + it.next());
            }
        }
    }

//
//    public static void execOnService(String serviceUrl, String inputStr) {
//        Document inDoc = Parser.parse(inputStr);
//        Document b = GraphQlUtils.applyTransform(inDoc, new TransformExpandShortcuts());
//        Document preprocessedDoc = GraphQlUtils.applyTransform(b, TransformAssignGlobalIds.of("state_", 0));
//
//        System.out.println(AstPrinter.printAst(preprocessedDoc));
//
//        // graphql.language.Field.newField().build().transform(builder -> builder.add)
//
////        Function<Field, P_Path0> fieldToKey = field -> {
////        	Directive directive = field.getDirectives("emitRdfKey");
////        	XGraphQlUtils.parseEmitRdfKey(null)
////        };
//
//        // assertTransformResult(inputStr, expectedStr, new TransformExpandShortcuts());
//        // assertTransformResult(inputStr, expectedStr, new GraphQlToSparqlConverter());
//        // RewriteResult rewriteResult = new RewriteResult();
//        // Map<Class<?>, Object> rootVars = new HashMap<>();
//        // rootVars.put(RewriteResult.class, rewriteResult);
//
//
//
//        ObjectNotationWriterViaGon<?, ?> backend;
//        ObjectNotationWriter<P_Path0, Node> writer;
//
//        if (false) {
//            ObjectNotationWriterViaGon<P_Path0, Node> tmp = ObjectNotationWriterViaGon.of(GonProviderJava.newInstance());
//            // Plain java
//            writer = tmp;
//        } else {
//            GonProvider<String, JsonPrimitive> gsonProvider = GonProviderGson.of();
//            ObjectNotationWriterViaGon<String, JsonPrimitive> delegate = ObjectNotationWriterViaGon.of(gsonProvider);
//            backend = delegate;
//            writer = new ObjectNotationWriterMapper<>(delegate, gsonProvider, RdfElementVisitorRdfToJsonNt::path0ToName,
//            RdfElementVisitorRdfToJsonNt::nodeToJsonElement);
//        }
//
//
//
//        NodeTraverser nodeTraverser = new NodeTraverser(); //rootVars, Node::getChildren);
//        GraphQlToSparqlConverter<P_Path0> graphqlToSparqlConverter = new GraphQlToSparqlConverter<>(
//                // XGraphQlUtils::fieldToRdfKey
//                XGraphQlUtils::fieldToJsonKey
//                );
//
//        // RewriteResult rewriteResult = (RewriteResult)nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
//        nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
//        RewriteResult<Binding, FunctionEnv, P_Path0, org.apache.jena.graph.Node> rewriteResult = graphqlToSparqlConverter.getRewriteResult();
//
//        for (Entry<String, SingleResult<Binding, FunctionEnv, P_Path0, org.apache.jena.graph.Node>> entry : rewriteResult.map().entrySet()) {
//            System.out.println("Processing: " + entry.getKey());
//            SingleResult<Binding, FunctionEnv, P_Path0, org.apache.jena.graph.Node> single = entry.getValue();
//            AggStateGon<Binding, FunctionEnv, P_Path0, org.apache.jena.graph.Node> agg = single.rootAggBuilder().newAggregator();
//            ElementNode elementNode = single.rootElementNode();
//
//            Function<Query, QueryExec> qtoe = query -> QueryExec.service(serviceUrl).query(query).build();
//
//            try (GraphQlFieldExec<P_Path0, org.apache.jena.graph.Node> exec = GraphQlExecUtils2.exec(elementNode, agg, qtoe, single.isSingle())) {
//                exec.sendNextItemToWriter(writer);
//                // Object product = exec.nextItem();
//
//                Object product = backend.getProduct();
//                if (product instanceof JsonElement je) {
//                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                    System.out.println(gson.toJson(je));
//                } else {
//                    System.out.println(product);
//                }
//
//            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

//
//
//    public static void transformBottomUp(Document document) {
//        String query = "query { users { id name } }";
//
//        // Parse the query to an AST
//        NodeTraverser nodeTraverser = new NodeTraverser();
//        // nodeTraverser.p
//        nodeTraverser.postOrder(new GraphQlToSparqlConverter(), document);
//    }

    public static void assertTransformResult(String inputStr, String expectedStr, NodeVisitorStub transform) {
        Document expectedDoc = Parser.parse(expectedStr);

        Document inputDoc = Parser.parse(inputStr);
        Document actualDoc = GraphQlUtils.applyTransform(inputDoc, transform);
        System.out.println(AstPrinter.printAst(actualDoc));

        Assert.assertTrue(AstComparator.isEqual(actualDoc, expectedDoc));
    }

}



//@Override
//public TraversalControl visitField(Field node, TraverserContext<Node> context) {
//  List<Directive> directives = node.getDirectives();
//  List<Directive> mergedDirectives = mergeNameDirectives(directives);
//
//  if (!directives.equals(mergedDirectives)) {
//      Field newNode = node.transform(builder -> builder.directives(mergedDirectives));
//      context.changeNode(newNode);
//  }
//  return TraversalControl.CONTINUE;
//}

//private List<Directive> mergeNameDirectives(List<Directive> directives) {
//  List<Directive> newDirectives = new ArrayList<>();
//  List<Argument> nameArguments = new ArrayList<>();
//
//  for (Directive directive : directives) {
//      if (directive.getName().equals("name")) {
//          nameArguments.addAll(directive.getArguments());
//      } else {
//          newDirectives.add(directive);
//      }
//  }
//
//  if (!nameArguments.isEmpty()) {
//      List<String> values = new ArrayList<>();
//      for (Argument argument : nameArguments) {
//          if (argument.getValue() instanceof StringValue) {
//              values.add(((StringValue) argument.getValue()).getValue());
//          } else if (argument.getValue() instanceof ArrayValue) {
//              for (Value<?> value : ((ArrayValue) argument.getValue()).getValues()) {
//                  if (value instanceof StringValue) {
//                      values.add(((StringValue) value).getValue());
//                  }
//              }
//          }
//      }
//
//      List<Value> valueNodes = values.stream().distinct()
//              .map(StringValue::new)
//              .collect(Collectors.toList());
//      Argument newArgument = Argument.newArgument()
//              .name("value")
//              .value(ArrayValue.newArrayValue().values(valueNodes).build())
//              .build();
//
//      Directive newDirective = Directive.newDirective()
//              .name("name")
//              .arguments(List.of(newArgument))
//              .build();
//      newDirectives.add(newDirective);
//  }
//
//  return newDirectives;
//}

