package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterInMemory;
import org.apache.jena.graph.Node;

/**
 * Execution for producing the results for a field and its sub-tree in a graphql document.
 * In the initial design, this interface was intended for generating the results for a top-level field,
 * but now it is also used for producing all results for a document.
 * (The document is strictly speaking not a field in graphql terminology, so perhaps it should be renamed to GraphQlNodeExec).
 */
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

    /**
     * Write out data for the extension block of a graphql response.
     * This can be used to expose additional information, such as the underlying
     * SPARQL query.
     *
     * <pre>
     * {
     *   "data": ...
     *   "extensions": {
     *      "sparqlQuery": "SELECT * { ... }"
     *   }
     * }
     * <pre>
     *
     * @param writer The writer in object state - i.e. {@code .beginObject()} must have been called.
     * @throws IOException
     */
    public void writeExtensions(ObjectNotationWriter<K, Node> writer, Function<String, K> stringToKey) throws IOException;


    /**
     * Return an iterator over objects based on {@link #sendNextItemToWriter(ObjectNotationWriter)}.
     * The input data is materalized using the in-memory writer and the produced objects are those
     * returned by the iterator.
     */
    default <T> Iterator<T> asIterator(ObjectNotationWriterInMemory<T, K, Node> inMemoryWriter) {
        return new GraphQlFieldExecIterator<>(this, inMemoryWriter);
    }
}
