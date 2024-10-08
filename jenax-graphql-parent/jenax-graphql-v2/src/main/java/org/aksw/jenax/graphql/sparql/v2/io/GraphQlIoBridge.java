package org.aksw.jenax.graphql.sparql.v2.io;

import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExec;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderApi;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderRon;
import org.aksw.jenax.graphql.sparql.v2.ron.RdfElement;
import org.aksw.jenax.graphql.sparql.v2.ron.RdfLiteral;
import org.aksw.jenax.graphql.sparql.v2.ron.RdfLiteralImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.util.NodeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Methods to create sinks for use with {@link GraphQlExec#sendNextItemToWriter(ObjectNotationWriter)}.
 */
public class GraphQlIoBridge {
    /** RDF literals with this JSON datatype will be embedded in the JSON output. */
    public static String DATATYPE_IRI_JSON = "https://w3id.org/aksw/norse#json";

    // ronSinkBuilder.mapKey().mapValue()

    // public static <T, V> ObjectNotationWriterAdapter<P_Path0, String, Node, V> adaptRon(GonProviderApi<T, String, V> ronToJson) {
    public static <T, V> ObjectNotationWriterInMemory<T, P_Path0, Node> bridgeRonToJsonInMemory(GonProviderApi<T, String, V> jsonProvider) {
        ObjectNotationWriterInMemory<T, String, V> destination = ObjectNotationWriterViaGon.of(jsonProvider);

        GonProvider<String, V> gonProvider = jsonProvider;
        Function<P_Path0, String> keyMapper = GraphQlIoBridge::path0ToStrOrNt;
        Function<Node, V> valueMapper = node -> (V)GraphQlIoBridge.nodeToGon(NodeFactory.createLiteralString(NodeFmtLib.strNT(node)), jsonProvider);

        ObjectNotationWriterMapper<P_Path0, String, Node, V> writer = new ObjectNotationWriterMapperImpl<>(destination, gonProvider, keyMapper, valueMapper);
        ObjectNotationWriter<P_Path0, Node> front = writer;

        ObjectNotationWriterInMemory<T, P_Path0, Node> result = new ObjectNotationWriterInMemoryWrapper<T, P_Path0, Node>(front, destination);

        return result;
    }

    public static ObjectNotationWriterInMemory<RdfElement, P_Path0, Node> bridgeRonToRdfElement() {
        GonProviderApi<RdfElement, P_Path0, RdfLiteral> gonProvider = GonProviderRon.getInstance();
        ObjectNotationWriterInMemory<RdfElement, P_Path0, RdfLiteral> destination = ObjectNotationWriterViaGon.of(gonProvider);

        Function<P_Path0, P_Path0> keyMapper = x -> x;
        Function<Node, RdfLiteral> valueMapper = RdfLiteralImpl::new;

        ObjectNotationWriterMapper<P_Path0, P_Path0, Node, RdfLiteral> writer = new ObjectNotationWriterMapperImpl<>(destination, gonProvider, keyMapper, valueMapper);
        ObjectNotationWriter<P_Path0, Node> front = writer;

        ObjectNotationWriterInMemory<RdfElement, P_Path0, Node> result = new ObjectNotationWriterInMemoryWrapper<RdfElement, P_Path0, Node>(front, destination);
        return result;
    }

    public static <T, V> ObjectNotationWriterInMemory<T, String, Node> bridgeToJsonInMemory(GonProviderApi<T, String, V> jsonProvider) {
        ObjectNotationWriterInMemory<T, String, V> destination = ObjectNotationWriterViaGon.of(jsonProvider);

        GonProvider<String, V> gonProvider = jsonProvider;
        Function<String, String> keyMapper = x -> x;
        Function<Node, V> valueMapper = node -> (V)GraphQlIoBridge.nodeToGon(node, jsonProvider);

        ObjectNotationWriterMapper<String, String, Node, V> writer = new ObjectNotationWriterMapperImpl<>(destination, gonProvider, keyMapper, valueMapper);
        ObjectNotationWriter<String, Node> front = writer;

        ObjectNotationWriterInMemory<T, String, Node> result = new ObjectNotationWriterInMemoryWrapper<T, String, Node>(front, destination);

        return result;
    }

    // exec.sendNextItemToWriter(ObjectNotationWriter<P_Path0, Node> writer)
    // exec.adapt(bridge).sendNextItemToWriter(ObjectNotationWriter<String, V> writer)
//    public static <T, V> ObjectNotationWriter<T, String, Node> bridgeRonToJsonFn(ObjectNotationWriter<P_Path0, Node> before) {
//        ObjectNotationWriterInMemory<T, String, V> destination = ObjectNotationWriterViaGon.of(jsonProvider);
//
//        GonProvider<String, V> gonProvider = jsonProvider;
//        Function<P_Path0, String> keyMapper = GraphQlIoBridge::path0ToName;;
//        Function<Node, V> valueMapper = node -> (V)GraphQlIoBridge.nodeToGon(node, jsonProvider);
//
//        ObjectNotationWriterMapper<P_Path0, String, Node, V> writer = new ObjectNotationWriterMapperImpl<>(destination, gonProvider, keyMapper, valueMapper);
//        ObjectNotationWriter<P_Path0, Node> front = writer;
//
//        ObjectNotationWriterInMemory<T, P_Path0, Node> result = new ObjectNotationWriterInMemoryWrapper<T, P_Path0, Node>(front, destination);
//
//        return result;
//    }

    public static <T, V> T nodeToGon(Node node, GonProviderApi<T, ?, V> gonProvider) {
        T result;
        if (node == null) {
            result = gonProvider.newNull();
        } else if (node.isURI()) {
            result = (T)gonProvider.newLiteral(node.getURI());
        } else if (node.isLiteral()) {
            Object obj = node.getLiteralValue();
            //boolean isNumber =//NodeMapperRdfDatatype.canMapCore(node, Number.class);
            //if(isNumber) {
            // if (obj instanceof JsonElement) {
                // Case for any datatype with native Json representation - including out datatype.
                // result = gonProvider.upcast(obj);
            if (DATATYPE_IRI_JSON.equals(node.getLiteralDatatypeURI())) {
                // Fallback if our JSON datatype is not registered
                // The datatype would store JSON as JsonElement
                String lex = node.getLiteralLexicalForm();

                Gson gson = new Gson();
                try {
                    // result = gson.fromJson(lex, JsonElement.class);
                    result = gonProvider.parse(lex);
                } catch (Exception e) {
                    // TODO Log warning
                    // If JSON parsing failed then use the string representation after all.
                    result = (T)gonProvider.newLiteral(lex);
                }
            } else if (obj instanceof String) {
                String value = (String)obj;
                result = (T)gonProvider.newLiteral(value);
            } else if (obj instanceof Number) {
                Number value = (Number)obj; //NodeMapperRdfDatatype.toJavaCore(node, Number.class);
                result = (T)gonProvider.newLiteral(value);
            } else if (obj instanceof Boolean) {
                Boolean value = (Boolean) obj;
                result = (T)gonProvider.newLiteral(value);
            } else {
                String value = node.getLiteralLexicalForm(); // Objects.toString(obj);
                result = (T)gonProvider.newLiteral(value);; //+ "^^" + obj.getClass().getCanonicalName());
        //		throw new RuntimeException("Unsupported literal: " + rdfNode);
            }
        } else if (node.isBlank()) {
            String value = node.getBlankNodeLabel();
            result = (T)gonProvider.newLiteral(value);
        } else {
            String value = node.toString();
            result = (T)gonProvider.newLiteral(value);
        }
        return result;
    }

    public static JsonElement nodeToJsonElement(Node node) {
        JsonElement result;
        if (node == null) {
            result = JsonNull.INSTANCE;
        } else if (node.isURI()) {
            result = new JsonPrimitive(node.getURI());
        } else if (node.isLiteral()) {
            Object obj = node.getLiteralValue();
            //boolean isNumber =//NodeMapperRdfDatatype.canMapCore(node, Number.class);
            //if(isNumber) {
            // if (obj instanceof JsonElement) {
                // Case for any datatype with native Json representation - including out datatype.
                // result = gonProvider.upcast(obj);
            if ("https://w3id.org/aksw/norse#json".equals(node.getLiteralDatatypeURI())) {
                // Fallback if our JSON datatype is not registered
                // The datatype would store JSON as JsonElement
                String lex = node.getLiteralLexicalForm();

                Gson gson = new Gson();
                try {
                    result = gson.fromJson(lex, JsonElement.class);
                } catch (Exception e) {
                    // TODO Log warning
                    // If JSON parsing failed then use the string representation after all.
                    result = new JsonPrimitive(lex);
                }
            } else if (obj instanceof String) {
                String value = (String)obj;
                result = new JsonPrimitive(value);
            } else if (obj instanceof Number) {
                Number value = (Number)obj; //NodeMapperRdfDatatype.toJavaCore(node, Number.class);
                result = new JsonPrimitive(value);
            } else if (obj instanceof Boolean) {
                Boolean value = (Boolean) obj;
                result = new JsonPrimitive(value);
            } else {
                String value = node.getLiteralLexicalForm(); // Objects.toString(obj);
                result = new JsonPrimitive(value); //+ "^^" + obj.getClass().getCanonicalName());
        //		throw new RuntimeException("Unsupported literal: " + rdfNode);
            }
        } else if (node.isBlank()) {
            String value = node.getBlankNodeLabel();
            result = new JsonPrimitive(value);
        } else {
            String value = node.toString();
            result = new JsonPrimitive(value);
        }
        return result;
    }

    public static String path0ToStrNt(P_Path0 path) {
        boolean isFwd = path.isForward();
        Node node = path.getNode();
        String name = (!isFwd ? "^" : "") + NodeFmtLib.strNT(node);
        return name;
    }

    public static String path0ToStrOrNt(P_Path0 path) {
        boolean isFwd = path.isForward();
        Node node = path.getNode();
        boolean isString = NodeUtils.isSimpleString(node) || NodeUtils.isLangString(node);
        String name = (!isFwd ? "^" : "") + (isString ? node.getLiteralLexicalForm() : NodeFmtLib.strNT(node));
        return name;
    }

    public static String path0ToName(P_Path0 path) {
        boolean isFwd = path.isForward();
        Node node = path.getNode();
        String name = (!isFwd ? "^" : "") + getPlainString(node);
        return name;
    }

    public static String getPlainString(Node node) {
        return node == null
            ? "(null)"
            : node.isURI()
                ? node.getURI()
                : node.isBlank()
                    ? node.getBlankNodeLabel()
                    : node.isLiteral()
                        ? node.getLiteralLexicalForm()
                        : node.toString();
    }
}
