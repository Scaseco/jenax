package org.aksw.jenax.io.rdf.json;

import java.math.BigDecimal;
import java.util.function.Function;

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
public interface JsonProvider {
    // Should we require the id of an object to be set in advance?
    Object newObject();
    boolean isObject(Object obj);

    /** Get the id of an object.
     *  Can be thought of as extracting the {@code @id} attribute of a JSON-LD object. */
    Node getObjectId(Object obj);
    void setProperty(Object obj, String p, Object value);
    Object getProperty(Object obj, String p);
    void removeProperty(Object obj,String p);

    default Object computePropertyIfAbsent(Object obj, String p, Function<String, Object> fn) {
        Object result = getProperty(obj, p);
        if (result == null) {
            result = fn.apply(p);
            ensureValidJson(result);
            setProperty(obj, p, result);
        }
        return result;
    }

    default Object getOrCreateObject(Object parent, String p) {
        return computePropertyIfAbsent(parent, p, k -> newObject());
    }


    void ensureValidJson(Object obj);

    Object newArray();
    boolean isArray(Object obj);
    void addElement(Object arr, Object value);
    void setElement(Object arr, int index, Object value);
    void removeElement(Object arr, int index);

    Object newLiteral(String value);
    Object newLiteral(boolean value);
    Object newLiteral(long value);
    Object newLiteral(double value);
    Object newLiteral(BigDecimal value);
    boolean isLiteral(Object obj);
    Node getLiteral(Object obj);

    Object newNull();
    boolean isNull(Object obj);
}
