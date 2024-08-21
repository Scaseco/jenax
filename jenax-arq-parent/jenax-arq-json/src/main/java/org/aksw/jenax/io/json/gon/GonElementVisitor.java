package org.aksw.jenax.io.json.gon;

public interface GonElementVisitor<K, V, T> {
    T visit(GonArray<K, V> element);
    T visit(GonObject<K, V> element);
    T visit(GonLiteral<K, V> element);
    T visit(GonNull<K, V> element);
}
