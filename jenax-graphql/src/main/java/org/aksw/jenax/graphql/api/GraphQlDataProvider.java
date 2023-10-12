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

    /** Write the data of this provider to the given json writer */
    void write(JsonWriter writer, Gson gson) throws IOException; // IOException?
}
