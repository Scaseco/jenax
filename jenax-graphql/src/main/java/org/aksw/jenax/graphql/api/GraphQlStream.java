package org.aksw.jenax.graphql.api;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface GraphQlStream {
    /** The name of the provider. Usable as a key in the JSON result. */
    String getName();

    /** Metadata for this stream. */
    JsonObject getMetadata();
    Stream<JsonElement> openStream();
}
