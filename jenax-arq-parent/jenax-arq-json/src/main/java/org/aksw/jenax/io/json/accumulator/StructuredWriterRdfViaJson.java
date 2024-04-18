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

public class StructuredWriterRdfViaJson
    implements StructuredWriterRdf
{
    protected Gson gson;
    protected JsonWriter jsonWriter;

    public StructuredWriterRdfViaJson(Gson gson, JsonWriter jsonWriter) {
        super();
        this.gson = gson;
        this.jsonWriter = jsonWriter;
    }

    @Override
    public void flush() throws IOException {
        jsonWriter.flush();
    }

    @Override
    public StructuredWriterRdf beginArray() throws IOException {
        jsonWriter.beginArray();
        return this;
    }

    @Override
    public StructuredWriterRdf endArray() throws IOException {
        jsonWriter.endArray();
        return this;
    }

    @Override
    public StructuredWriterRdf beginObject() throws IOException {
        jsonWriter.beginObject();
        return this;
    }

    @Override
    public StructuredWriterRdf endObject() throws IOException {
        jsonWriter.endObject();
        return this;
    }

    @Override
    public StructuredWriterRdf name(Node name) throws IOException {
        String str = nodeToJsonKey(name);
        jsonWriter.name(str);
        return this;
    }

    @Override
    public StructuredWriterRdf value(Node value) throws IOException {
        JsonElement elt = toJson(value);
        gson.toJson(elt, jsonWriter);
        return this;
    }

    @Override
    public StructuredWriterRdf nullValue() throws IOException {
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
