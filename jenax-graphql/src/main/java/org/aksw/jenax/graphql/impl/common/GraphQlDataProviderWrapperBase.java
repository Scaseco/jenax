package org.aksw.jenax.graphql.impl.common;

import java.io.IOException;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public class GraphQlDataProviderWrapperBase
    implements GraphQlDataProvider
{
    protected GraphQlDataProvider delegate;

    public GraphQlDataProviderWrapperBase(GraphQlDataProvider delegate) {
        super();
        this.delegate = delegate;
    }

    public GraphQlDataProvider getDelegate() {
        return delegate;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public JsonObject getMetadata() {
        return getDelegate().getMetadata();
    }

    @Override
    public Stream<JsonElement> openStream() {
        return getDelegate().openStream();
    }

    @Override
    public boolean isSingle() {
        return getDelegate().isSingle();
    }

    @Override
    public void write(JsonWriter writer, Gson gson) throws IOException {
        getDelegate().write(writer, gson);
    }

    @Override
    public String toString() {
        return "Wrapped: " + getDelegate().toString();
    }
}
