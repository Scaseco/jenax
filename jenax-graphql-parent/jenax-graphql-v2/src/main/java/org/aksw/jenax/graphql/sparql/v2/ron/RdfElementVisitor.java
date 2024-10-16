package org.aksw.jenax.graphql.sparql.v2.ron;

public interface RdfElementVisitor<T> {
    T visit(RdfArray element);
    T visit(RdfObject element);
    T visit(RdfLiteral element);
    T visit(RdfNull element);
}
