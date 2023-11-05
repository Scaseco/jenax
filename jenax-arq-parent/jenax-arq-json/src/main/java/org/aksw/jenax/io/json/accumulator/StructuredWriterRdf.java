package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.apache.jena.graph.Node;

public interface StructuredWriterRdf
    extends StructuredWriter
{
    @Override
    StructuredWriterRdf beginArray() throws IOException;

    @Override
    StructuredWriterRdf endArray() throws IOException;

    @Override
    StructuredWriterRdf beginObject() throws IOException;

    @Override
    StructuredWriterRdf endObject() throws IOException;

    StructuredWriterRdf name(Node name) throws IOException;
    StructuredWriterRdf value(Node value) throws IOException;

    StructuredWriterRdf nullValue() throws IOException;
}
