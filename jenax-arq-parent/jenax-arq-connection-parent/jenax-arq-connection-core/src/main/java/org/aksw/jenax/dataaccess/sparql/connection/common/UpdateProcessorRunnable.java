package org.aksw.jenax.dataaccess.sparql.connection.common;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorRunnable
    implements UpdateProcessor
{
    protected Context context;
    protected DatasetGraph datasetGraph;
    protected Runnable delegate;

    public UpdateProcessorRunnable(Context context, DatasetGraph datasetGraph, Runnable delegate) {
        super();
        this.context = context;
        this.datasetGraph = datasetGraph;
        this.delegate = delegate;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void abort() {
        // no op
    }

//	@Override
//	public DatasetGraph getDatasetGraph() {
//		return datasetGraph;
//	}

    @Override
    public void execute() {
        delegate.run();
    }
}
