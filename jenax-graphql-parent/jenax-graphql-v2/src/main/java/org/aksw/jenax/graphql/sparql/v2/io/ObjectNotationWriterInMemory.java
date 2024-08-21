package org.aksw.jenax.graphql.sparql.v2.io;

public interface ObjectNotationWriterInMemory<T, K, V>
    extends ObjectNotationWriter<K, V>
{
    T getProduct();
    void clear();
}
