package org.aksw.jenax.io.json.accumulator;

import com.google.gson.JsonElement;

/**
 * The motivation for this abstraction was to have a JsonWriter which can assemble JsonObjects.
 * It turns out that guava has the JsonTreeWriter (an internal but public class) which does the job.
 */
public interface JsonWriterGson {

    JsonWriterGson beginArray() throws Exception;

    JsonWriterGson endArray() throws Exception;

    JsonWriterGson beginObject() throws Exception;

    JsonWriterGson endObject() throws Exception;

    JsonWriterGson name(String name) throws Exception;

    JsonWriterGson value(Boolean value) throws Exception;

    JsonWriterGson value(Number value) throws Exception;

    JsonWriterGson value(boolean value) throws Exception;

    JsonWriterGson value(double value) throws Exception;

    JsonWriterGson value(long value) throws Exception;

    JsonWriterGson nullValue() throws Exception;

    JsonWriterGson value(String value) throws Exception;

    /** Extension */
    JsonWriterGson toJson(JsonElement value) throws Exception;
}
