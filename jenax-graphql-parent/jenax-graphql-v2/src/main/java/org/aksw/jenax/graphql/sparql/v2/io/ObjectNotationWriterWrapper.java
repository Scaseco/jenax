package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;

public interface ObjectNotationWriterWrapper<K, V>
    extends ObjectNotationWriter<K, V>
{
    ObjectNotationWriter<K, V> getDelegate();

    @Override default void flush() throws IOException
    { getDelegate().flush(); }

    @Override default ObjectNotationWriter<K, V> beginArray() throws IOException
    { getDelegate().beginArray(); return this; }

    @Override default ObjectNotationWriter<K, V> endArray() throws IOException
    { getDelegate().endArray(); return this; }

    @Override default ObjectNotationWriter<K, V> beginObject() throws IOException
    { getDelegate().beginObject(); return this; }

    @Override default ObjectNotationWriter<K, V> endObject() throws IOException
    { getDelegate().endObject(); return this; }

    @Override default ObjectNotationWriter<K, V> name(K key) throws IOException
    { getDelegate().name(key); return this; }

    /** Write a primitive value (should exclude null) */
    @Override default ObjectNotationWriter<K, V> value(V value) throws IOException
    { getDelegate().value(value); return this; }

    @Override default ObjectNotationWriter<K, V> nullValue() throws IOException
    { getDelegate().nullValue(); return this; }
}
