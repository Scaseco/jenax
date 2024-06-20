package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import com.google.gson.JsonElement;

/**
 * The motivation for this abstraction was to have a JsonWriter which can assemble JsonObjects.
 * It turns out that guava has the JsonTreeWriter (an internal but public class) which does the job.
 */
public interface JsonWriterGson
    extends ObjectNotationWriter
{
    @Override
    JsonWriterGson beginArray() throws IOException;

    @Override
    JsonWriterGson endArray() throws IOException;

    @Override
    JsonWriterGson beginObject() throws IOException;

    @Override
    JsonWriterGson endObject() throws IOException;

    JsonWriterGson name(String name) throws IOException;

    JsonWriterGson value(Boolean value) throws IOException;

    JsonWriterGson value(Number value) throws IOException;

    JsonWriterGson value(boolean value) throws IOException;

    JsonWriterGson value(double value) throws IOException;

    JsonWriterGson value(long value) throws IOException;

    JsonWriterGson nullValue() throws IOException;

    JsonWriterGson value(String value) throws IOException;

    /** Extension */
    JsonWriterGson toJson(JsonElement value) throws IOException;
}
