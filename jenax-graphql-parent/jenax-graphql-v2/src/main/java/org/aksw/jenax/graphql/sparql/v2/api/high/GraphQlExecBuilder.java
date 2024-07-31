package org.aksw.jenax.graphql.sparql.v2.api.high;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlFieldExec;
import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlProcessor;
import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlProcessorSettings;
import org.aksw.jenax.graphql.sparql.v2.api.low.RdfGraphQlProcessorFactoryImpl;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.exec.QueryExecBuilder;

import graphql.language.Document;

public class GraphQlExecBuilder
    implements GraphQlProcessorSettings<GraphQlExecBuilder>
{
    protected Creator<QueryExecBuilder> queryExecBuilderFactory;

    protected Document document;
    protected Map<String, Node> assignments = new LinkedHashMap<>(); // Binding?

    public GraphQlExecBuilder(Creator<QueryExecBuilder> queryExecBuilderFactory) {
        super();
        this.queryExecBuilderFactory = Objects.requireNonNull(queryExecBuilderFactory);
    }

    @Override
    public GraphQlExecBuilder document(Document document) {
        this.document = document;
        return this;
    }

    @Override
    public GraphQlExecBuilder setVar(String name, Node value) {
        this.assignments.put(name, value);
        return this;
    }

    public GraphQlExec<String> build() {
        GraphQlProcessor<String> processor = RdfGraphQlProcessorFactoryImpl.forJson().newBuilder().document(document).setVars(assignments).build();
        GraphQlFieldExec<String> exec = processor.newExecBuilder().service(queryExecBuilderFactory).build();
        return new GraphQlExec<>(exec);
    }
}
