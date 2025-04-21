package org.aksw.jenax.graphql.sparql.v2.gon.model;

import org.aksw.jenax.graphql.sparql.v2.ron.RdfObject;

/**
 * A data model for RDF tree structures akin to gson's JsonElement.
 */
public interface GonElement<K, V> {

    default boolean isArray() {
        return this instanceof GonArray;
    }

    default GonArray<K, V> getAsArray() {
        return (GonArray<K, V>)this;
    }

    default boolean isObject() {
        return this instanceof RdfObject;
    }

    default GonObject<K, V> getAsObject() {
        return (GonObject<K, V>)this;
    }

    default boolean isLiteral() {
        return this instanceof GonLiteral;
    }

    default GonLiteral<K, V> getAsLiteral() {
        return (GonLiteral<K, V>)this;
    }

    default boolean isNull() {
        return this instanceof GonNull;
    }

    default GonNull<K, V> asNull() {
        return (GonNull<K, V>)this;
    }

    <T> T accept(GonElementVisitor<K, V, T> visitor);

    ParentLink<K, V> getParent();

    default void unlinkFromParent() {
        ParentLink<K, V> link = getParent();

        if (link != null) {
            if (link.isObjectLink()) {
                ParentLinkObject<K, V> objLink = link.asObjectLink();
                K key = objLink.getKey();
                objLink.getParent().remove(key);
            } else if (link.isArrayLink()) {
                ParentLinkArray<K, V> arrLink = link.asArrayLink();
                int index = arrLink.getIndex();
                // objLink.getParent().remove(index);
                arrLink.getParent().set(index, new GonNull<>());
            } else {
                throw new RuntimeException("Unknown parent link type: " + link.getClass());
            }
        } else {
            // Ignore
            // throw new RuntimeException("Cannot unlink an element that does not have a parent");
        }
    }

    default GonElement<K, V> getRoot() {
        ParentLink<K, V> link = getParent();
        GonElement<K, V> result = link == null ? this : link.getParent().getRoot();
        return result;
    }


//    public static RdfElement of(Node node) {
//        RdfElement result = node == null
//            ? RdfNull.get()
//            : RdfElement.newObject(node);
//        return result;
//    }

//    public static RdfElement newObject(Node node) {
//        return new RdfObjectImpl(node);
//    }
//
//    public static RdfArray newArray() {
//        return new RdfArrayImpl();
//    }
//
//    public static RdfElement newLiteral(Node node) {
//        return new RdfLiteralImpl(node);
//    }
//
//    public static RdfElement nullValue() {
//        return RdfNull.get();
//    }
}
