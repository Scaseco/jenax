package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GonProviderJava<K, V>
    implements GonProviderApi<Object, K, V>
{
    public static <K, V> GonProviderJava<K, V> newInstance() {
        return new GonProviderJava<>();
    }

    @Override
    public Object upcast(Object element) {
        return element;
    }

    @Override
    public Object parse(String str) {
        throw new UnsupportedOperationException("Parsing not supported. Use e.g. a GonProviderGson for JSON serialization and parsing.");
    }

    @Override
    public Object newObject() {
        return new LinkedHashMap<K, Object>();
    }

    @Override
    public boolean isObject(Object obj) {
        return obj instanceof LinkedHashMap;
    }

    @SuppressWarnings("unchecked")
    protected Map<K, Object> asObjectMap(Object obj) {
        return (Map<K, Object>)obj;
    }

    @Override
    public void setProperty(Object obj, K key, Object value) {
        asObjectMap(obj).put(key, value);
    }

    @Override
    public Object getProperty(Object obj, Object key) {
        return asObjectMap(obj).get(key);
    }

    @Override
    public void removeProperty(Object obj, Object key) {
        asObjectMap(obj).remove(key);
    }

    @Override
    public Iterator<Entry<K, Object>> listProperties(Object obj) {
        return asObjectMap(obj).entrySet().iterator();
    }

    @SuppressWarnings("unused")
    @Override
    public Object newArray() {
        return new ArrayList<Object>();
    }

    @Override
    public boolean isArray(Object obj) {
        return obj instanceof ArrayList;
    }


    @SuppressWarnings("unchecked")
    protected List<Object> asGonList(Object obj) {
        return (List<Object>)obj;
    }

    @Override
    public void addElement(Object arr, Object value) {
        asGonList(arr).add(value);
    }

    @Override
    public void setElement(Object arr, int index, Object value) {
        asGonList(arr).set(index, value);
    }

    @Override
    public void removeElement(Object arr, int index) {
        asGonList(arr).remove(index);
    }

    @Override
    public Iterator<Object> listElements(Object arr) {
        return asGonList(arr).iterator();
    }

    @Override
    public Object newDirectLiteral(Object value) {
        return value;
    }

    @Override
    public V newLiteral(boolean value) {
        return (V)Boolean.valueOf(value);
    }

    @Override
    public V newLiteral(Number value) {
        return (V)value;
    }

    @Override
    public V newLiteral(String value) {
        return (V)value;
    }

    @Override
    public boolean isLiteral(Object obj) {
        return !isArray(obj) && !isObject(obj) && !isNull(obj);
    }

//    @SuppressWarnings("unchecked")
//    @Override
//    public V getLiteral(Object obj) {
//        return (V)obj;
//    }

    @SuppressWarnings("unchecked")
    @Override
    public V getLiteral(Object obj) {
        return (V)obj;
    }

    @Override
    public Object newNull() {
        return null;
    }

    @Override
    public boolean isNull(Object obj) {
        return obj == null;
    }
}
