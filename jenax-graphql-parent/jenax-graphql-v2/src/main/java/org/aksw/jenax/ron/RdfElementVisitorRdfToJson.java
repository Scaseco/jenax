package org.aksw.jenax.ron;

import java.util.Map.Entry;

import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.aksw.jenax.graphql.sparql.v2.io.RdfObjectNotationWriterViaJson;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 *
 * If rdfTermMode is true, then all json literals will be strings that
 * contain RDF terms in n-triples syntax.
 *
 */
public class RdfElementVisitorRdfToJson
    implements RdfElementVisitor<JsonElement>
{
    // protected boolean rdfTermMode;

//    public RdfElementVisitorRdfToJson() {
//        this(false);
//    }
//
//    public RdfElementVisitorRdfToJson(boolean rdfTermMode) {
//        super();
//        this.rdfTermMode = rdfTermMode;
//    }

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

            // TODO Add the object's node
            Node id = obj.getExternalId();
//            if (id != null && rdfTermMode) {
//                result.addProperty("@id", NodeFmtLib.strNT(id));
//            }
//
//            String name = rdfTermMode
//                    ? NodeFmtLib.strNT(key)
//                    : RdfObjectNotationWriterViaJson.nodeToJsonKey(key);

            String name = RdfObjectNotationWriterViaJson.nodeToJsonKey(key);
            JsonElement elt = e.getValue().accept(this);
            result.add(name, elt);
        }
        return result;
    }

    @Override
    public JsonElement visit(RdfLiteral element) {
        Node node = element.getInternalId();
//        JsonElement result = rdfTermMode
//                ? new JsonPrimitive(NodeFmtLib.strNT(node))
//                : RdfObjectNotationWriterViaJson.toJson(node);
        JsonElement result = GraphQlIoBridge.nodeToJsonElement(node);
        return result;
    }

    @Override
    public JsonElement visit(RdfNull element) {
        return JsonNull.INSTANCE;
    }
}
