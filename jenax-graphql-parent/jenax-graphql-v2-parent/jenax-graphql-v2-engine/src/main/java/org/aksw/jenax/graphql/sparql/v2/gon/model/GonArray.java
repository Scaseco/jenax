package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface GonArray<K, V>
    extends GonElement<K, V>, Iterable<GonElement<K, V>>
{
    int size();
    GonElement<K, V> get(int index);
    GonArray<K, V> add(GonElement<K, V> element);

    GonArray<K, V> set(int index, GonElement<K, V> element);
    GonArray<K, V> remove(int index);
}
