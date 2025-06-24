package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface ParentLinkObject<K, V>
    extends ParentLink<K, V>
{
    @Override
    GonObject<K, V> getParent();

    K getKey();
}
