package org.aksw.jenax.dataaccess.sparql.execution.update;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public interface UpdateProcessorWrapper
    extends UpdateProcessor
{
    UpdateProcessor getDelegate();

    default Context getContext() {
        return getDelegate().getContext();
    }

    default DatasetGraph getDatasetGraph() {
        return getDelegate().getDatasetGraph();
    }

    default void execute() {
        getDelegate().execute();
    }
}
