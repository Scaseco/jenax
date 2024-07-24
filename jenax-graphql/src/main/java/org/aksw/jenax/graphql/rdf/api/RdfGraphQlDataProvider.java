package org.aksw.jenax.graphql.rdf.api;

import java.io.IOException;
import java.util.stream.Stream;

import org.aksw.jenax.io.json.accumulator.RdfObjectNotationWriter;
import org.aksw.jenax.io.rdf.json.RdfElement;

import com.google.gson.JsonObject;

public interface RdfGraphQlDataProvider
{
    /** The name of the provider. Usable as a key in the JSON result. */
    String getName();

    /** Metadata for this stream. */
    // Resource getMetadata();
    JsonObject getMetadata();

    /** A stream over the resulting items. */
    Stream<RdfElement> openStream();

    /**
     * Whether this provider is expected to yield at most 1 result.
     * The client can use this information to e.g. omit starting an array in the output.
     * However, the data provider does not know whether this information is truthful.
     * If a violation is encountered during runtime then an exception will be raised.
     */
    boolean isSingle();

    /** Write the data of this provider to the given json writer */
    // void write(JsonWriter writer, Gson gson) throws IOException; // IOException?
    void write(RdfObjectNotationWriter writer) throws IOException; // IOException?
}
