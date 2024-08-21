package org.aksw.jenax.io.json.gon;

public interface ParentLinkObject<K, V>
    extends ParentLink<K, V>
{
    @Override
    GonObject<K, V> getParent();

    K getKey();
}
