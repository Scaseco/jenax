package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

/**
 * A data model for RDF tree structures akin to gson's JsonElement.
 */
public interface RdfElement {
    default boolean isArray() {
        return this instanceof RdfArray;
    }

    default RdfArray getAsArray() {
        return (RdfArray)this;
    }

    default boolean isObject() {
        return this instanceof RdfObject;
    }

    default RdfObject getAsObject() {
        return (RdfObject)this;
    }

    default boolean isLiteral() {
        return this instanceof RdfLiteral;
    }

    default RdfLiteral getAsLiteral() {
        return (RdfLiteral)this;
    }

    default boolean isNull() {
        return this instanceof RdfNull;
    }

    default RdfNull asNull() {
        return (RdfNull)this;
    }

    <T> T accept(RdfElementVisitor<T> visitor);

//    public static RdfElement of(Node node) {
//        RdfElement result = node == null
//            ? RdfNull.get()
//            : RdfElement.newObject(node);
//        return result;
//    }

    public static RdfElement newObject(Node node) {
        return new RdfObject(node);
    }

    public static RdfArray newArray() {
        return new RdfArray();
    }

    public static RdfElement newLiteral(Node node) {
        return new RdfLiteral(node);
    }

    public static RdfElement nullValue() {
        return RdfNull.get();
    }
}
