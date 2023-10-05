package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public interface UpdateExecDecorator
    extends UpdateExec
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
