package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.Iterator;
import java.util.Map.Entry;

public interface GonProviderWrapper<K, V>
    extends GonProvider<K, V>
{
    GonProvider<K, V> getDelegate();

    @Override
    default Object parse(String str) {
        return getDelegate().parse(str);
    }

    @Override
    default Object newObject() {
        return getDelegate().newObject();
    }

    @Override
    default boolean isObject(Object obj) {
        return getDelegate().isObject(obj);
    }

    @Override
    default void setProperty(Object obj, K key, Object value) {
        getDelegate().setProperty(obj, key, value);
    }

    @Override
    default Object getProperty(Object obj, Object key) {
        return getDelegate().getProperty(obj, key);
    }

    @Override
    default void removeProperty(Object obj, Object key) {
        getDelegate().removeProperty(obj, key);
    }

    @Override
    default Iterator<Entry<K, Object>> listProperties(Object obj) {
        return getDelegate().listProperties(obj);
    }

    @Override
    default Object newArray() {
        return getDelegate().newArray();
    }

    @Override
    default boolean isArray(Object obj) {
        return getDelegate().isArray(obj);
    }

    @Override
    default void addElement(Object arr, Object value) {
        getDelegate().addElement(arr, value);
    }

    @Override
    default void setElement(Object arr, int index, Object value) {
        getDelegate().setElement(arr, index, value);
    }

    @Override
    default void removeElement(Object arr, int index) {
        getDelegate().removeElement(arr, index);
    }

    @Override
    default Iterator<Object> listElements(Object arr) {
        return getDelegate().listElements(arr);
    }

    @Override
    default Object newDirectLiteral(V value) {
        return getDelegate().newDirectLiteral(value);
    }

    @Override
    default V newLiteral(boolean value) {
        return getDelegate().newLiteral(value);
    }

    @Override
    default V newLiteral(Number value) {
        return getDelegate().newLiteral(value);
    }

    @Override
    default V newLiteral(String value) {
        return getDelegate().newLiteral(value);
    }

    @Override
    default boolean isLiteral(Object obj) {
        return getDelegate().isLiteral(obj);
    }

    @Override
    default V getLiteral(Object obj) {
        return getDelegate().getLiteral(obj);
    }

    @Override
    default Object newNull() {
        return getDelegate().newNull();
    }

    @Override
    default boolean isNull(Object obj) {
        return getDelegate().isNull(obj);
    }
}
