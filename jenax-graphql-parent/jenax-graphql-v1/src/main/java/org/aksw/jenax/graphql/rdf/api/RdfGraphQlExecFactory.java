package org.aksw.jenax.graphql.rdf.api;

import java.util.Map;

import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.rdf.adapter.GraphQlExecFactoryOverRdf;
import org.aksw.jenax.ron.RdfElement;
import org.apache.jena.graph.Node;

import graphql.language.Document;

/** An {@link RdfGraphQlExecFactory} is a factory for {@link RdfGraphQlExecBuilder} instances which
 *  in turn produce {@link RdfGraphQlExec} instances.
 *
 *  The {@link #toJson()} method provides a plain JSON view instead of the more general {@link RdfElement} view.
 *
 *  Default shorthands are provided that delegate to the builder.
 */
public interface RdfGraphQlExecFactory {
    RdfGraphQlExecBuilder newBuilder();

    default RdfGraphQlExec create(Document document) {
        return newBuilder().setDocument(document).build();
    }

    default RdfGraphQlExec create(Document document, Map<String, Node> assignments) {
        return newBuilder().setDocument(document).setAssignments(assignments).build();
    }

    default RdfGraphQlExec create(String documentString) {
        return newBuilder().setDocument(documentString).build();
    }

    default RdfGraphQlExec create(String documentString, Map<String, Node> assignments) {
        return newBuilder().setDocument(documentString).setAssignments(assignments).build();
    }

    default GraphQlExecFactory toJson() {
        return new GraphQlExecFactoryOverRdf(this);
    }
}
