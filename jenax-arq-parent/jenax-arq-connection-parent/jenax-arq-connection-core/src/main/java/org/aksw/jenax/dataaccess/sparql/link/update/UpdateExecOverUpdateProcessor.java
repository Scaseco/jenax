package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.Objects;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public class UpdateExecOverUpdateProcessor
    implements UpdateExec
{
    protected UpdateProcessor delegate;

    protected UpdateExecOverUpdateProcessor(UpdateProcessor delegate) {
        super();
        this.delegate = delegate;
    }

    public static UpdateExec adapt(UpdateProcessor delegate) {
        Objects.requireNonNull(delegate);
        return delegate instanceof UpdateExec
                ? (UpdateExec) delegate
                : new UpdateExecOverUpdateProcessor(delegate);
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public void abort() {
        delegate.abort();
    }

    @Override
    public void execute() {
        delegate.execute();
    }

//  @Override
//  public DatasetGraph getDatasetGraph() {
//      return delegate.getDatasetGraph();
//  }
}
