package org.aksw.jenax.ron;

public interface RdfElementVisitor<T> {
    T visit(RdfArray element);
    T visit(RdfObject element);
    T visit(RdfLiteral element);
    T visit(RdfNull element);
}
