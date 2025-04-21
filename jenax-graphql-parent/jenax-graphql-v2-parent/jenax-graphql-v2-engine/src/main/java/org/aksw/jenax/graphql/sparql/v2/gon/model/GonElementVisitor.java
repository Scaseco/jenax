package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface GonElementVisitor<K, V, T> {
    T visit(GonArray<K, V> element);
    T visit(GonObject<K, V> element);
    T visit(GonLiteral<K, V> element);
    T visit(GonNull<K, V> element);
}
