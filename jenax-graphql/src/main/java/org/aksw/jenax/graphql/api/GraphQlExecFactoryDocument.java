package org.aksw.jenax.graphql.api;

import graphql.language.Document;

/** Core low-level interface for {@link Document}-based factories of {@link GraphQlExec} instances. */
@FunctionalInterface
public interface GraphQlExecFactoryDocument {
    /**
     * A typical graphql JSON request has the attributes {@code { "query": "...", "variables": { ... } "operationName": "..." } }
     * This method accounts for the parsed value of the 'query' field.
     */
    GraphQlExec create(Document document);

    /** XXX Add builder API to control aspects such as timeout */
}
