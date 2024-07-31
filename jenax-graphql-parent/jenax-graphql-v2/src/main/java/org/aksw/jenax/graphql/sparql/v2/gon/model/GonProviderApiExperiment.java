package org.aksw.jenax.graphql.sparql.v2.gon.model;

//public interface GonProviderApiExperiment<K, V, T>
//    extends GonProvider<K, V>
//{
//    // Should we require the id of an object to be set in advance?
//    Object $newObject();
//    boolean $isObject(Object obj);
//
//    void $setProperty(T obj, K key, T value);
//    Object $getProperty(T obj, K key);
//    void $removeProperty(T obj, K key);
//    Iterator<Entry<K, Object>> $listProperties(T obj);
//
//    Object $newArray();
//    boolean $isArray(Object obj);
//    void $addElement(Object arr, Object value);
//    void $setElement(Object arr, int index, Object value);
//    void $removeElement(Object arr, int index);
//    Iterator<Object> $listElements(Object arr);
//
//    Object $newLiteral(V value);
//
//    /** Non-null literal. */
//    boolean $isLiteral(Object obj);
//    V $getLiteral(Object obj);
//
//    Object $newNull();
//    boolean $isNull(Object obj);
//
//    T upcast(Object obj);
//
//    @Override
//    T parse(String str);
//
//    @Override
//    T newObject();
//
//    default void setProperty(Object obj, K key, Object value) {
//        T $obj = upcast(obj);
//        T $val = upcast(value);
//        $setProperty($obj, key, $val);
//    }
//
//    default T getProperty(Object obj, Object key) {
//    	$getProperty
//    }
//    void removeProperty(Object obj, Object key);
//    Iterator<Entry<K, Object>> listProperties(Object obj);
//
//    Object newArray();
//    boolean isArray(Object obj);
//    void addElement(Object arr, Object value);
//    void setElement(Object arr, int index, Object value);
//    void removeElement(Object arr, int index);
//    Iterator<Object> listElements(Object arr);
//
//    Object newLiteral(V value);
//
//    /** Non-null literal. */
//    boolean isLiteral(Object obj);
//    V getLiteral(Object obj);
//
//    Object newNull();
//    boolean isNull(Object obj);
//}
