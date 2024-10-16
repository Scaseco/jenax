package org.aksw.jenax.io.json.gon;

public interface ParentLinkArray<K, V>
    extends ParentLink<K, V>
{
    @Override
    GonArray<K, V> getParent();

    int getIndex();
}
