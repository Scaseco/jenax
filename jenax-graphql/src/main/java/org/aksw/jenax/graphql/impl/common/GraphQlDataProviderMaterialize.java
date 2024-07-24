package org.aksw.jenax.graphql.impl.common;

import java.io.IOException;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

/**
 * Wrapper that implements {@link #write(JsonWriter, Gson)} against {@link #openStream()}.
 */
public class GraphQlDataProviderMaterialize
    extends GraphQlDataProviderWrapperBase
{
    public GraphQlDataProviderMaterialize(GraphQlDataProvider delegate) {
        super(delegate);
    }

    @Override
    public void write(JsonWriter writer, Gson gson) throws IOException {
        boolean debugMaterialization = false;
        if (debugMaterialization) {
            try (Stream<JsonElement> stream = openStream()) {
                writer.beginArray();
                stream.forEach(elt -> gson.toJson(elt, writer));
                writer.endArray();
            }
            return;
        }
    }
}
