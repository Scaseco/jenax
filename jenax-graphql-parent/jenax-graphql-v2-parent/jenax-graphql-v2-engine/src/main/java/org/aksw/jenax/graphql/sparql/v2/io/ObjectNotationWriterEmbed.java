package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;

/** Writer with support for embedding values. */
@Deprecated // Embedding is more easily handled on the SPARQL engine:
// JSON literals are always embedded, if this is undesired then simply convert to string in SPARQL
public interface ObjectNotationWriterEmbed<K, V>
    extends ObjectNotationWriter<K, V>
{
    public enum EmbedFailurePolicy {
        LITERAL, // Write the value out as a literal
        WARN,    // Report a warning
        ERROR,   // Report an error
        ABORT    // Quit processing
    }

    ObjectNotationWriter<K, V> embedValue(V value, EmbedFailurePolicy failurePolicy) throws IOException;
}
