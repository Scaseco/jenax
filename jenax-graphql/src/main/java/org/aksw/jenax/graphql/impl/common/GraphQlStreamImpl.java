package org.aksw.jenax.graphql.impl.common;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.api.GraphQlStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GraphQlStreamImpl
    implements GraphQlStream
{
    protected String name;
    protected JsonObject metadata;
    protected Supplier<Stream<JsonElement>> streamSupplier;

    public GraphQlStreamImpl(String name, JsonObject extensions, Supplier<Stream<JsonElement>> streamSupplier) {
        super();
        this.name = name;
        this.metadata = extensions;
        this.streamSupplier = streamSupplier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JsonObject getMetadata() {
        return metadata;
    }

    @Override
    public Stream<JsonElement> openStream() {
        return streamSupplier.get();
    }
}
