package org.aksw.jenax.io.rowset.core;

import java.io.Flushable;
import java.io.IOException;

import org.apache.jena.sparql.engine.binding.Binding;

public interface RowSetStreamWriter
    extends Flushable, AutoCloseable
{
    @Override
    void close() throws IOException;

    void writeAskResult(boolean askResult) throws IOException;

    void writeHeader() throws IOException;
    void beginBindings();
    void writeBindingSeparator();
    // For some formats, such as JSON, we need to know whether to emit a record separator.
    void writeBinding(Binding binding)  throws IOException;
    void endBindings();
    void writeFooter() throws IOException;
}
