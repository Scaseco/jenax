package org.aksw.jenax.graphql.sparql.v2.gon.model;

public class ParentLinkObjectImpl<K, V>
    implements ParentLinkObject<K, V>
{
    private final GonObject<K, V> parent;
    private final K key;

    public ParentLinkObjectImpl(GonObject<K, V> parent, K key) {
        super();
        this.parent = parent;
        this.key = key;
    }

    @Override
    public GonObject<K, V> getParent() {
        return parent;
    }

    @Override
    public K getKey() {
        return key;
    }
}
