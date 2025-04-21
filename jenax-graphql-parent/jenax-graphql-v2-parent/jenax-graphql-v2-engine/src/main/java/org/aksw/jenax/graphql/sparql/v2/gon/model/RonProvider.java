package org.aksw.jenax.graphql.sparql.v2.gon.model;

import org.apache.jena.graph.Node;

/**
 * Abstraction akin to JSONPath's JsonProvidere.
 * There are subtle differences between JSON and RON: In RON, keys and literals are all RDF terms.
 *
 * @param <K> The key type of objects. The type correspondences are strings for JSON and RDF terms for RON.
 * @param <V> The type of values contained in literals. Typically object for JSON and RDF term for RON.
 *
 * If we allowed a V type, then we'd need a Java-to-V mapper, such as Object-to-Node.
 */
public interface RonProvider {
    // Should we require the id of an object to be set in advance?
    Object newObject(Node node);
    boolean isObject(Object obj);

    /** Get the id of an object.
     *  Can be thought of as extracting the {@code @id} attribute of a JSON-LD object. */
    Node getObjectId(Object obj);
    void setProperty(Object obj, Node p, boolean isFoward, Object value);
    Object getProperty(Object obj, Node p, boolean isForward);
    void removeProperty(Object obj, Node p, boolean isForward);

    Object newArray();
    boolean isArray(Object obj);
    void addElement(Object arr, Object value);
    void setElement(Object arr, int index, Object value);
    void removeElement(Object arr, int index);

    Object newLiteral(Node node);
    boolean isLiteral(Object obj);
    Node getLiteral(Object obj);

    Object newNull();
    boolean isNull(Object obj);
}
