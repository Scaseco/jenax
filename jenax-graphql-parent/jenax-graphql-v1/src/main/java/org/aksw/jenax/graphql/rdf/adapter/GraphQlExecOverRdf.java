package org.aksw.jenax.graphql.rdf.adapter;

import java.util.Set;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlDataProvider;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;
import org.aksw.jenax.ron.RdfElementVisitor;
import org.aksw.jenax.ron.RdfElementVisitorRdfToJson;

import com.google.gson.JsonElement;

public class GraphQlExecOverRdf
    implements GraphQlExec
{
    protected RdfGraphQlExec delegate;
    protected RdfElementVisitor<JsonElement> converter;

    public GraphQlExecOverRdf(RdfGraphQlExec delegate) {
        this(delegate, new RdfElementVisitorRdfToJson());
    }

    public GraphQlExecOverRdf(RdfGraphQlExec delegate, RdfElementVisitor<JsonElement> converter) {
        super();
        this.delegate = delegate;
        this.converter = converter;
    }

    @Override
    public Set<String> getDataProviderNames() {
        return delegate.getDataProviderNames();
    }

    @Override
    public GraphQlDataProvider getDataProvider(String name) {
        RdfGraphQlDataProvider base = delegate.getDataProvider(name);
        GraphQlDataProvider result = base == null ? null : new GraphQlDataProviderOverRdf(base, converter);
        return result;
    }
}
