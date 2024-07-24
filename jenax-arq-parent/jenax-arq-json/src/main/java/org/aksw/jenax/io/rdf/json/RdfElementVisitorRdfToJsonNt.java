package org.aksw.jenax.io.rdf.json;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathLib;

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
}
