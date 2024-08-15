package org.aksw.jenax.io.json.accumulator;

import java.io.Flushable;
import java.io.IOException;

/**
 * Object notation writers support objects (aka "associative arrays", "dictionaries", "maps")
 * and arrays. Methods for writing values are provided by specializations.
 */
public interface ObjectNotationWriter
    extends Flushable
{
    ObjectNotationWriter beginArray() throws IOException;
    ObjectNotationWriter endArray() throws IOException;
    ObjectNotationWriter beginObject() throws IOException;
    ObjectNotationWriter endObject() throws IOException;
}
