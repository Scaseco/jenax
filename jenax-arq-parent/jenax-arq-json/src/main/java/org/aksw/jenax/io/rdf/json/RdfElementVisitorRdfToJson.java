package org.aksw.jenax.io.rdf.json;

import java.util.Map.Entry;

import org.aksw.jenax.io.json.accumulator.RdfObjectNotationWriterViaJson;
import org.apache.jena.graph.Node;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class RdfElementVisitorRdfToJson
    implements RdfElementVisitor<JsonElement>
{
    protected Gson gson;

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
        for (Entry<Node, RdfElement> e : obj.getMembers().entrySet()) {
            Node key = e.getKey();
            String name = RdfObjectNotationWriterViaJson.nodeToJsonKey(key);
            JsonElement elt = e.getValue().accept(this);
            result.add(name, elt);
        }
        return result;
    }

    @Override
    public JsonElement visit(RdfLiteral element) {
        Node node = element.getNode();
        JsonElement result = RdfObjectNotationWriterViaJson.toJson(node);
        return result;
    }

    @Override
    public JsonElement visit(RdfNull element) {
        return JsonNull.INSTANCE;
    }
}
