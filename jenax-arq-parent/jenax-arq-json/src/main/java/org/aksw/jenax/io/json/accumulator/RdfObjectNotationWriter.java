package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public interface RdfObjectNotationWriter
    extends ObjectNotationWriter
{
    @Override
    RdfObjectNotationWriter beginArray() throws IOException;

    @Override
    RdfObjectNotationWriter endArray() throws IOException;

    @Override
    RdfObjectNotationWriter beginObject() throws IOException;

    @Override
    RdfObjectNotationWriter endObject() throws IOException;

    // RdfObjectNotationWriter name(Node name, boolean isForward) throws IOException;
    RdfObjectNotationWriter name(P_Path0 name) throws IOException;

    RdfObjectNotationWriter value(Node value) throws IOException;

    RdfObjectNotationWriter nullValue() throws IOException;
}
