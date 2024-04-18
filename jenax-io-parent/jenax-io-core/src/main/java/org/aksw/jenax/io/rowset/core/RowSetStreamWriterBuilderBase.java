package org.aksw.jenax.io.rowset.core;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.Context;

import com.google.common.base.Preconditions;

public abstract class RowSetStreamWriterBuilderBase
    implements RowSetStreamWriterBuilder
{
    protected List<Var> vars;

    protected OutputStream outputStream;
    protected Writer writer;
    protected Context context;
    protected NodeFormatter nodeFormatter;

    /** Abstract outputStream or writer as an AWriter. Returns null if neither is set. */
    protected AWriter getAWriter() {
        AWriter result = outputStream != null
                ? IO.wrapUTF8(outputStream)
                : writer != null
                    ? IO.wrap(writer)
                    : null;
        return result;
    }

    @Override
    public RowSetStreamWriterBuilder setOutput(OutputStream output) {
        this.outputStream = output;
        this.writer = null;
        return this;
    }

    @Override
    public RowSetStreamWriterBuilder setOutput(Writer output) {
        this.writer = output;
        this.outputStream = null;
        return this;
    }

    @Override
    public RowSetStreamWriterBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    public RowSetStreamWriterBuilder setVars(List<Var> vars) {
        this.vars = vars;
        return this;
    }

    @Override
    public RowSetStreamWriterBuilder setNodeFormatter(NodeFormatter nodeFormatter) {
        this.nodeFormatter = nodeFormatter;
        return this;
    }

    @Override
    public RowSetStreamWriter build() {
        verify();
        RowSetStreamWriter result = buildActual();
        return result;
    }

    protected void verify() {
        Preconditions.checkArgument(outputStream != null || writer != null, "No output was set");
    }

    protected abstract RowSetStreamWriter buildActual();
}
