package org.aksw.jenax.graphql.sparql.v2.ron;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Json-NT is simply json where keys and literals are all strings that contain
 * RDF terms in n-triples serialization.
 * The format is sub-par from a parsing perspective because the document needs to be parsed first as
 * JSON and then each string literal as an RDF term.
 * However, other than that it is very easy to work with.
 */
public class RdfElementVisitorRdfToJsonNt
    implements RdfElementVisitor<JsonElement>
{
    @Override
    public JsonElement visit(RdfArray elements) {
        JsonArray result = new JsonArray();
        for (RdfElement item : elements) {
            JsonElement elt = item.accept(this);
            result.add(elt);
        }
        return result;
    }

    @Override
    public JsonElement visit(RdfObject obj) {
        JsonObject result = new JsonObject();
        for (Entry<P_Path0, RdfElement> e : obj.getMembers().entrySet()) {
            P_Path0 path = e.getKey();
            Node key = path.getNode();
            boolean isFwd = path.isForward();

            // Add the object's node
            Node id = obj.getExternalId();
            if (id != null) {
                result.addProperty("@id", NodeFmtLib.strNT(id));
            }

            String name = (!isFwd ? "^" : "") + NodeFmtLib.strNT(key);
            // String name = path.toString();
            JsonElement elt = e.getValue().accept(this);
            result.add(name, elt);
        }
        return result;
    }

    @Override
    public JsonElement visit(RdfLiteral element) {
        Node node = element.getInternalId();
        JsonElement result = new JsonPrimitive(NodeFmtLib.strNT(node));
        return result;
    }

    @Override
    public JsonElement visit(RdfNull element) {
        return JsonNull.INSTANCE;
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


    public static String path0ToNt(P_Path0 path) {
        boolean isFwd = path.isForward();
        Node node = path.getNode();
        String name = (!isFwd ? "^" : "") + NodeFmtLib.strNT(node);
        return name;
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
            if (obj instanceof JsonElement) {
                // Case for any datatype with native Json representation - including out datatype.
                result = (JsonElement)obj;
            } else if ("https://w3id.org/aksw/norse#json".equals(node.getLiteralDatatypeURI())) {
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
                result = new JsonPrimitive(value) ; //+ "^^" + obj.getClass().getCanonicalName());
        //		throw new RuntimeException("Unsupported literal: " + rdfNode);
            }
        } else if (node.isBlank()) {
            result = new JsonPrimitive(node.getBlankNodeLabel());
        } else {
            result = new JsonPrimitive(node.toString());
        }
        return result;
    }
}
