package org.aksw.jenax.dataaccess.sparql.execution.update;

import org.apache.jena.update.UpdateProcessor;

public interface UpdateProcessorWrapper
    extends UpdateProcessor
{
    UpdateProcessor getDelegate();

//    default Context getContext() {
//        return getDelegate().getContext();
//    }
//
//    default DatasetGraph getDatasetGraph() {
//        return getDelegate().getDatasetGraph();
//    }

    @Override
    default void execute() {
        getDelegate().execute();
    }
}
