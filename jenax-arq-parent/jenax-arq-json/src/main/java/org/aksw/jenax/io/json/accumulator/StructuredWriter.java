package org.aksw.jenax.io.json.accumulator;

import java.io.Flushable;
import java.io.IOException;

public interface StructuredWriter
    extends Flushable
{
    StructuredWriter beginArray() throws IOException;
    StructuredWriter endArray() throws IOException;
    StructuredWriter beginObject() throws IOException;
    StructuredWriter endObject() throws IOException;
}
