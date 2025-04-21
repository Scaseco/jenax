package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The motivation for this abstraction was to have a JsonWriter which can assemble JsonObjects.
 * It turns out that guava has the JsonTreeWriter (an internal but public class) which does the job.
 */
public interface JsonWriterGson
    extends ObjectNotationWriterExt<String, JsonPrimitive, JsonWriterGson>
{
    @Override
    default JsonWriterGson value(JsonPrimitive value) throws IOException {
        if (value.isString()) {
            value(value.getAsString());
        } else if (value.isNumber()) {
            value(value.getAsNumber());
        } else if (value.isBoolean()) {
            value(value.getAsBoolean());
        } else if (value.isJsonNull()) {
            nullValue();
        } else {
            throw new IllegalArgumentException("Unknown JSON primitive: " + value);
        }
        return this;
    }

    JsonWriterGson value(Boolean value) throws IOException;

    JsonWriterGson value(Number value) throws IOException;

    JsonWriterGson value(boolean value) throws IOException;

    JsonWriterGson value(double value) throws IOException;

    JsonWriterGson value(long value) throws IOException;

    JsonWriterGson value(String value) throws IOException;

    /** Extension */
    JsonWriterGson toJson(JsonElement value) throws IOException;
}
