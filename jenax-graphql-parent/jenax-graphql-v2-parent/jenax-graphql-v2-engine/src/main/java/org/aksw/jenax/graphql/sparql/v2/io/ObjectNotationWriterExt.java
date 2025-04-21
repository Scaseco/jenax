package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;

public interface ObjectNotationWriterExt<K, V, X extends ObjectNotationWriterExt<K, V, X>>
    extends ObjectNotationWriter<K, V>
{
    @Override X beginArray() throws IOException;
    @Override X endArray() throws IOException;
    @Override X beginObject() throws IOException;
    @Override X endObject() throws IOException;

    @Override X name(K key) throws IOException;
    @Override X value(V value) throws IOException;
    @Override X nullValue() throws IOException;
}
