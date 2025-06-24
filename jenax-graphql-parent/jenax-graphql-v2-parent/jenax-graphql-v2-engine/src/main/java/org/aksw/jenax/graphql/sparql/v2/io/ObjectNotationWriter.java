package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.Flushable;
import java.io.IOException;

/**
 * Object notation writers support objects (aka "associative arrays", "dictionaries", "maps")
 * and arrays. Methods for writing values are provided by specializations.
 */
public interface ObjectNotationWriter<K, V>
    extends Flushable
{
    ObjectNotationWriter<K, V> beginArray() throws IOException;
    ObjectNotationWriter<K, V> endArray() throws IOException;

    ObjectNotationWriter<K, V> beginObject() throws IOException;
    ObjectNotationWriter<K, V> endObject() throws IOException;

    ObjectNotationWriter<K, V> name(K key) throws IOException;

    /** Write a primitive value (should exclude null) */
    ObjectNotationWriter<K, V> value(V value) throws IOException;

    ObjectNotationWriter<K, V> nullValue() throws IOException;

    // XXX Method to write existing objects? Such as put a JSON object onto the writer.
    // Could be used to place whole objects into an in-memory sink.
    // writeObject(Object obj)

    // XXX Specific method for null? Could use value(null) instead.
    // X nullValue() throws IOException;
}
