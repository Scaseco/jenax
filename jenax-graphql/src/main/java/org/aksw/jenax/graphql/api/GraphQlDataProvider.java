package org.aksw.jenax.graphql.api;

import java.io.IOException;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public interface GraphQlDataProvider {
    /** The name of the provider. Usable as a key in the JSON result. */
    String getName();

    /** Metadata for this stream. */
    JsonObject getMetadata();
    Stream<JsonElement> openStream();

    /**
     * Whether this provider is expected to yield at most 1 result.
     * The client can use this information to e.g. omit starting an array in the output.
     * However, the data provider does not know whether this information is truthful.
     * If a violation is encountered during runtime then an exception will be raised.
     */
    boolean isSingle();

    /** Write the data of this provider to the given json writer */
    void write(JsonWriter writer, Gson gson) throws IOException; // IOException?
}
