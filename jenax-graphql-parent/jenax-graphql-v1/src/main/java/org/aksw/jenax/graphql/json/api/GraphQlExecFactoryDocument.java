package org.aksw.jenax.graphql.json.api;

import java.util.Map;

import graphql.language.Document;
import graphql.language.Value;

/** Core low-level interface for {@link Document}-based factories of {@link GraphQlExec} instances. */
@FunctionalInterface
public interface GraphQlExecFactoryDocument {
    /**
     * A typical graphql JSON request has the attributes {@code { "query": "...", "variables": { ... } "operationName": "..." } }
     * This method accounts for the parsed value of the 'query' field.
     */
    GraphQlExec create(Document document, Map<String, Value<?>> assignments);

    /** XXX Add builder API to control aspects such as timeout */
}
