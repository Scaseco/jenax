package org.aksw.jenax.graphql.sparql.v2.exec.api.high;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public class GraphQlExecFactory {
    protected Creator<QueryExecBuilder> queryExecBuilderFactory;
    protected SchemaNavigator schemaNavigator;

    public GraphQlExecFactory(Creator<QueryExecBuilder> queryExecBuilderFactory, SchemaNavigator schemaNavigator) {
        super();
        this.queryExecBuilderFactory = Objects.requireNonNull(queryExecBuilderFactory);
        this.schemaNavigator = schemaNavigator;
    }

    public static GraphQlExecFactory of(Creator<QueryExecBuilder> queryExecBuilderFactory) {
        return of(queryExecBuilderFactory, null);
    }

    public static GraphQlExecFactory of(Creator<QueryExecBuilder> queryExecBuilderFactory, SchemaNavigator schemaNavigator) {
        return new GraphQlExecFactory(queryExecBuilderFactory, schemaNavigator);
    }

    public GraphQlExecBuilder newBuilder() {
        return new GraphQlExecBuilder()
            .queryExecBuilderFactory(queryExecBuilderFactory)
            .schemaNavigator(schemaNavigator);
    }
}
