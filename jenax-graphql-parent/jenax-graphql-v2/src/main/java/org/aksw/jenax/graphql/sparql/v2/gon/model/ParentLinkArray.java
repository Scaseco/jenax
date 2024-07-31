package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface ParentLinkArray<K, V>
    extends ParentLink<K, V>
{
    @Override
    GonArray<K, V> getParent();

    int getIndex();
}
