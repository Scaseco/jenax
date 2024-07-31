package org.aksw.jenax.graphql.rdf.api;

import java.util.Map;

import org.apache.jena.graph.Node;

import graphql.language.Document;

public interface RdfGraphQlExecBuilder {
    RdfGraphQlExecBuilder setDocument(Document document);
    RdfGraphQlExecBuilder setDocument(String documentString);

    // GraphQlExecBuilder setVar(String varName, Node value);
    RdfGraphQlExecBuilder setVar(String varName, Node value);
    RdfGraphQlExecBuilder setAssignments(Map<String, Node> assignments);

    /**
     * JSON Mode: If true, then the output will be the json model as specified in the graphql query.
     * If false, the result will be a RDF object tree model of the underling triples.
     */
    RdfGraphQlExecBuilder setJsonMode(boolean jsonMode);

    RdfGraphQlExec build();
}
