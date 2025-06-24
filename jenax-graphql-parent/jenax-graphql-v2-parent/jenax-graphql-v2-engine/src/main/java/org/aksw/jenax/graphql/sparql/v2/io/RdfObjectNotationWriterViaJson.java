package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

public class RdfObjectNotationWriterViaJson
    implements RdfObjectNotationWriter
{
    protected Gson gson;
    protected JsonWriter jsonWriter;
    // protected boolean rdfTermMode;

    public RdfObjectNotationWriterViaJson(Gson gson, JsonWriter jsonWriter) {
        this(gson, jsonWriter, false);
    }

    public RdfObjectNotationWriterViaJson(Gson gson, JsonWriter jsonWriter, boolean rdfTermMode) {
        super();
        this.gson = gson;
        this.jsonWriter = jsonWriter;
        // this.rdfTermMode = rdfTermMode;
    }


    @Override
    public void flush() throws IOException {
        jsonWriter.flush();
    }

    @Override
    public RdfObjectNotationWriter beginArray() throws IOException {
        jsonWriter.beginArray();
        return this;
    }

    @Override
    public RdfObjectNotationWriter endArray() throws IOException {
        jsonWriter.endArray();
        return this;
    }

    @Override
    public RdfObjectNotationWriter beginObject() throws IOException {
        jsonWriter.beginObject();
        return this;
    }

    @Override
    public RdfObjectNotationWriter endObject() throws IOException {
        jsonWriter.endObject();
        return this;
    }

    @Override
    public RdfObjectNotationWriter name(P_Path0 name) throws IOException {
        String str = nodeToJsonKey(name);
        jsonWriter.name(str);
        return this;
    }

    @Override
    public RdfObjectNotationWriter value(Node value) throws IOException {
        JsonElement elt = GraphQlIoBridge.nodeToJsonElement(value);
        gson.toJson(elt, jsonWriter);
        return this;
    }

    @Override
    public RdfObjectNotationWriter nullValue() throws IOException {
        jsonWriter.nullValue();
        return this;
    }

//    public static JsonElement toJson(Node value) {
//        JsonElement elt = value == null
//                ? JsonNull.INSTANCE
//                : value.isURI()
//                    ? new JsonPrimitive(value.getURI())
//                    : RdfJsonUtils.toJson(Graph.emptyGraph, value, 0, 1, false);
//        return elt;
//    }

    public static String nodeToJsonKey(P_Path0 name) {
        return nodeToJsonKey(name.getNode());
    }

    // FIXME Move this method to a common place
    public static String nodeToJsonKey(Node name) {
        String result;
        boolean strict = false;
        if (strict) {
            result = name.getLiteralLexicalForm();
        } else {
            result = name.isLiteral()
                    ? name.getLiteralLexicalForm() // Check for string literal
                    : name.isURI()
                        ? name.getURI()
                        : NodeFmtLib.strNT(name);
        }
        return result;
    }
}
