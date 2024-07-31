package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.io.IOException;
import java.util.Iterator;

import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterInMemory;
import org.apache.jena.graph.Node;

public interface GraphQlFieldExec<K>
    extends GraphQlExecCore
{
    /**
     * Send the next item to the writer. If {@link #isSingle()} is true then the whole response will be streamed.
     *
     * @param writer The object notation writer.
     * @return True iff any data was sent to the writer, false otherwise.
     * @throws IOException
     */
    boolean sendNextItemToWriter(ObjectNotationWriter<K, Node> writer) throws IOException;

//    default <T> Iterator<T> asIterator(GonProviderApi<T, K, Node> gonProvider) {
//        return new GraphQlFieldExecIterator<>(this, gonProvider);
//    }

    default <T> Iterator<T> asIterator(ObjectNotationWriterInMemory<T, K, Node> inMemoryWriter) {
        return new GraphQlFieldExecIterator<>(this, inMemoryWriter);
    }
}
