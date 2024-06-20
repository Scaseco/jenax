package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

public class RdfObjectNotationWriterViaJson
    implements RdfObjectNotationWriter
{
    protected Gson gson;
    protected JsonWriter jsonWriter;

    public RdfObjectNotationWriterViaJson(Gson gson, JsonWriter jsonWriter) {
        super();
        this.gson = gson;
        this.jsonWriter = jsonWriter;
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
    public RdfObjectNotationWriter name(Node name) throws IOException {
        String str = nodeToJsonKey(name);
        jsonWriter.name(str);
        return this;
    }

    @Override
    public RdfObjectNotationWriter value(Node value) throws IOException {
        JsonElement elt = toJson(value);
        gson.toJson(elt, jsonWriter);
        return this;
    }

    @Override
    public RdfObjectNotationWriter nullValue() throws IOException {
        jsonWriter.nullValue();
        return this;
    }

    public static JsonElement toJson(Node value) {
        JsonElement elt = value == null
                ? JsonNull.INSTANCE
                : value.isURI()
                    ? new JsonPrimitive(value.getURI())
                    : RdfJsonUtils.toJson(Graph.emptyGraph, value, 0, 1, false);
        return elt;
    }

    public static String nodeToJsonKey(Node name) {
        String result = name.getLiteralLexicalForm();
//        String result = name.isURI()
//                ? name.getURI()
//                : NodeFmtLib.strNT(name);
        return result;
    }
}
