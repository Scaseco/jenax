package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

public class JsonWriterAdapter
    implements JsonWriterGson
{
    protected JsonWriter delegate;
    protected Gson gson;

    protected JsonWriterAdapter(JsonWriter delegate, Gson gson) {
        super();
        this.delegate = delegate;
        this.gson = gson;
    }

    public static JsonWriterGson of(JsonWriter delegate, Gson gson) {
        return new JsonWriterAdapter(Objects.requireNonNull(delegate), gson);
    }

    @Override
    public JsonWriterGson beginArray() throws IOException {
        delegate.beginArray();
        return this;
    }

    @Override
    public JsonWriterGson endArray() throws IOException {
        delegate.endArray();
        return this;
    }

    @Override
    public JsonWriterGson beginObject() throws IOException {
        delegate.beginObject();
        return this;
    }

    @Override
    public JsonWriterGson endObject() throws IOException {
        delegate.endObject();
        return this;
    }

    @Override
    public JsonWriterGson name(String name) throws IOException {
        delegate.name(name);
        return this;
    }

    @Override
    public JsonWriterGson value(Boolean value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(Number value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(boolean value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(double value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(long value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson nullValue() throws IOException {
        delegate.nullValue();
        return this;
    }

    @Override
    public JsonWriterGson value(String value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson toJson(JsonElement value) throws IOException {
        gson.toJson(value, delegate);
        return this;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }
}
