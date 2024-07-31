package org.aksw.jenax.graphql.rdf.adapter;

import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecBuilder;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecBuilder;
import org.aksw.jenax.graphql.sparql.GraphQlUtils;

import graphql.language.Document;
import graphql.language.Value;

/**
 * JSON-view wrapper for an RdfGraphQlExecBuilder.
 * Enables jsonMode on the delegate when building the execution.
 */
public class GraphQlExecBuilderOverRdf
    implements GraphQlExecBuilder
{
    protected RdfGraphQlExecBuilder delegate;

    public GraphQlExecBuilderOverRdf(RdfGraphQlExecBuilder delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public GraphQlExecBuilder setDocument(Document document) {
        delegate.setDocument(document);
        return this;
    }

    @Override
    public GraphQlExecBuilder setDocument(String documentString) {
        delegate.setDocument(documentString);
        return this;
    }

    @Override
    public GraphQlExecBuilder setVar(String varName, Value<?> value) {
        delegate.setVar(varName, GraphQlUtils.toNodeValue(value).asNode());
        return this;
    }

    @Override
    public GraphQlExecBuilder setAssignments(Map<String, Value<?>> assignments) {
        delegate.setAssignments(GraphQlUtils.mapToJena(assignments));
        return this;
    }

    @Override
    public GraphQlExec build() {
        RdfGraphQlExecBuilder configuredDelegate = delegate.setJsonMode(true);
        return new GraphQlExecOverRdf(configuredDelegate.build());
    }
}
