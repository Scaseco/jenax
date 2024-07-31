package org.aksw.jenax.graphql.rdf.adapter;

import java.util.Objects;

import org.aksw.jenax.graphql.json.api.GraphQlExecBuilder;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecFactory;

public class GraphQlExecFactoryOverRdf
    implements GraphQlExecFactory
{
    protected RdfGraphQlExecFactory delegate;

    public GraphQlExecFactoryOverRdf(RdfGraphQlExecFactory delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public GraphQlExecBuilder newBuilder() {
        return new GraphQlExecBuilderOverRdf(delegate.newBuilder());
    }
}
