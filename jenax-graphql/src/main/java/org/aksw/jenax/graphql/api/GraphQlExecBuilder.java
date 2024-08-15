package org.aksw.jenax.graphql.api;

import org.apache.jena.graph.Node;

import graphql.language.Document;

public interface GraphQlExecBuilder {
    GraphQlExecBuilder setDocument(Document document);
    GraphQlExecBuilder setDocument(String documentString);

    GraphQlExecBuilder setVar(String varName, Node value);
}
