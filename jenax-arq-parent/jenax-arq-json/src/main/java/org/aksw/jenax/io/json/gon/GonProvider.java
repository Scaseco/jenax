package org.aksw.jenax.io.json.gon;

import java.util.Iterator;
import java.util.Map.Entry;

public interface GonProvider<K, V> {
    Object parse(String str);
    // XXX parse from InputStream

    // Should we require the id of an object to be set in advance?
    Object newObject();
    boolean isObject(Object obj);

    void setProperty(Object obj, K key, Object value);
    Object getProperty(Object obj, Object key);
    void removeProperty(Object obj, Object key);
    Iterator<Entry<K, Object>> listProperties(Object obj);

    Object newArray();
    boolean isArray(Object obj);
    void addElement(Object arr, Object value);
    void setElement(Object arr, int index, Object value);
    void removeElement(Object arr, int index);
    Iterator<Object> listElements(Object arr);

    Object newLiteral(V value);

    /** Non-null literal. */
    boolean isLiteral(Object obj);
    V getLiteral(Object obj);

    Object newNull();
    boolean isNull(Object obj);
}
