package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface GonLiteral<K, V>
    extends GonElement<K, V>
{
    V getValue();
}
