package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;

import com.google.gson.JsonObject;

public abstract class GraphQlDataProviderBase
    extends GraphQlDataProviderCommon
    implements GraphQlDataProvider
{
    public GraphQlDataProviderBase(String name, JsonObject extensions) {
        super(name, extensions);
    }
}
