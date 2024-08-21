package org.aksw.jenax.graphql.sparql.v2.api.high;

import java.util.Objects;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public class GraphQlExecFactory {
    protected Creator<QueryExecBuilder> queryExecBuilderFactory;

    public GraphQlExecFactory(Creator<QueryExecBuilder> queryExecBuilderFactory) {
        super();
        this.queryExecBuilderFactory = Objects.requireNonNull(queryExecBuilderFactory);
    }

    public static GraphQlExecFactory of(Creator<QueryExecBuilder> queryExecBuilderFactory) {
        return new GraphQlExecFactory(queryExecBuilderFactory);
    }

    public GraphQlExecBuilder newBuilder() {
        return new GraphQlExecBuilder(queryExecBuilderFactory);
    }
}
