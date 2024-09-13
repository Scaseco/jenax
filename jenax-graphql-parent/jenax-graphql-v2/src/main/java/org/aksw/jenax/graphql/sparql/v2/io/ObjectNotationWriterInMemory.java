package org.aksw.jenax.graphql.sparql.v2.io;

/**
 * @param <T> The object type of the product of a write operation. E.g. JsonElement.
 * @param <K> The key type of the object notation. Usually String but could be e.g. pairs of IRI and direction.
 * @param <V> The value type of the object notation. E.g. JsonElement or Jena's Node.
 */
public interface ObjectNotationWriterInMemory<T, K, V>
    extends ObjectNotationWriter<K, V>
{
    T getProduct();
    void clear();
}
