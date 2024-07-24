package org.aksw.jenax.graphql.rdf.api;

import org.aksw.jenax.graphql.impl.common.GraphQlDataProviderCommon;

import com.google.gson.JsonObject;

public abstract class RdfGraphQlDataProviderBase
    extends GraphQlDataProviderCommon
    implements RdfGraphQlDataProvider
{
    public RdfGraphQlDataProviderBase(String name, JsonObject extensions) {
        super(name, extensions);
    }
}
