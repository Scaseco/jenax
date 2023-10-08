package org.aksw.jenax.graphql;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface GraphQlStream {
    String getName();
    // Metadata for this stream
    JsonObject getMetadata();
    Stream<JsonElement> openStream();
}
