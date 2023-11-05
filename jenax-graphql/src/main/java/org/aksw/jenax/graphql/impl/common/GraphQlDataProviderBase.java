package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.api.GraphQlDataProvider;

import com.google.gson.JsonObject;

public abstract class GraphQlDataProviderBase
    implements GraphQlDataProvider
{
    protected String name;
    protected JsonObject metadata;

    public GraphQlDataProviderBase(String name, JsonObject extensions) {
        super();
        this.name = name;
        this.metadata = extensions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JsonObject getMetadata() {
        return metadata;
    }
}
