package org.aksw.jenax.io.json.accumulator;

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
    public JsonWriterGson beginArray() throws Exception {
        delegate.beginArray();
        return this;
    }

    @Override
    public JsonWriterGson endArray() throws Exception {
        delegate.endArray();
        return this;
    }

    @Override
    public JsonWriterGson beginObject() throws Exception {
        delegate.beginObject();
        return this;
    }

    @Override
    public JsonWriterGson endObject() throws Exception {
        delegate.endObject();
        return this;
    }

    @Override
    public JsonWriterGson name(String name) throws Exception {
        delegate.name(name);
        return this;
    }

    @Override
    public JsonWriterGson value(Boolean value) throws Exception {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(Number value) throws Exception {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(boolean value) throws Exception {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(double value) throws Exception {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson value(long value) throws Exception {
        delegate.value(value);
        return this;
    }
    @Override
    public JsonWriterGson nullValue() throws Exception {
        delegate.nullValue();
        return this;
    }

    @Override
    public JsonWriterGson value(String value) throws Exception {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriterGson toJson(JsonElement value) throws Exception {
        gson.toJson(value, delegate);
        return this;
    }
}
