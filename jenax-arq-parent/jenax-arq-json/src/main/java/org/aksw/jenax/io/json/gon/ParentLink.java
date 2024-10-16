package org.aksw.jenax.io.json.gon;

public interface ParentLink<K, V> {
    GonElement<K, V> getParent();

    default boolean isObjectLink() {
        return this instanceof ParentLinkObject;
    }

    default ParentLinkObject<K, V> asObjectLink() {
        return (ParentLinkObject<K, V>)this;
    }

    default boolean isArrayLink() {
        return this instanceof ParentLinkArray;
    }

    default ParentLinkArray<K, V> asArrayLink() {
        return (ParentLinkArray<K, V>)this;
    }
}
