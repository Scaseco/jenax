package org.aksw.jenax.graphql.impl.common;

import com.google.gson.JsonObject;

public abstract class GraphQlDataProviderCommon
{
    protected String name;
    protected JsonObject metadata;

    public GraphQlDataProviderCommon(String name, JsonObject extensions) {
        super();
        this.name = name;
        this.metadata = extensions;
    }

    // @Override
    public String getName() {
        return name;
    }

    // @Override
    public JsonObject getMetadata() {
        return metadata;
    }
}
