package org.aksw.jenax.graphql.sparql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecBuilder;
import org.apache.jena.graph.Node;

import graphql.language.Document;

public abstract class RdfGraphQlExecBuilderBase
    implements RdfGraphQlExecBuilder
{
    // Resolver is assumed to be configured on the GraphQlToSparqlMappingFactory
    // protected GraphQlResolver resolver;
    protected Document document;
    protected String documentString;
    protected Map<String, Node> assignments;
    protected boolean jsonMode = true;

    public RdfGraphQlExecBuilderBase() {
        super();
        this.assignments = new LinkedHashMap<>();
    }

//    public RdfGraphQlExecBuilder setResolver(GraphQlResolver resolver) {
//        this.resolver = resolver;
//        return this;
//    }

    @Override
    public RdfGraphQlExecBuilder setDocument(Document document) {
        Objects.requireNonNull(document);
        this.document = document;
        this.documentString = null;
        return this;
    }

    @Override
    public RdfGraphQlExecBuilder setDocument(String documentString) {
        Objects.requireNonNull(documentString);
        this.document = null;
        this.documentString = documentString;
        return this;
    }

    @Override
    public RdfGraphQlExecBuilder setVar(String varName, Node value) {
        assignments.put(varName, value);
        return this;
    }

    @Override
    public RdfGraphQlExecBuilder setAssignments(Map<String, Node> assignments) {
        if (assignments != null) {
            this.assignments.putAll(assignments);
        }
        return this;
    }

    @Override
    public RdfGraphQlExecBuilder setJsonMode(boolean jsonMode) {
        this.jsonMode = jsonMode;
        return this;
    }
}
