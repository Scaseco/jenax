package org.aksw.jenax.io.rowset.core;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.Context;

/**
 * Builder for {@link RowSetStreamWriter} instances.
 * Closing the RowSetStreamWriter also closes the underlying output stream or writer.
 */
public interface RowSetStreamWriterBuilder {
    RowSetStreamWriterBuilder setVars(List<Var> vars);

    RowSetStreamWriterBuilder setOutput(OutputStream output);
    RowSetStreamWriterBuilder setOutput(Writer output);

    RowSetStreamWriterBuilder setContext(Context context);
    RowSetStreamWriterBuilder setNodeFormatter(NodeFormatter nodeFormatter);

    RowSetStreamWriter build();
}
