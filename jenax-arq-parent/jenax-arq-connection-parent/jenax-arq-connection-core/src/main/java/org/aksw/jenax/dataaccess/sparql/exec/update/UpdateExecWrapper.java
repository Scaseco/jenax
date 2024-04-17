package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateProcessor;

public interface UpdateExecWrapper
    extends UpdateExec
{
    UpdateProcessor getDelegate();

//    @Override
//    default Context getContext() {
//        return getDelegate().getContext();
//    }

//    @Override
//    default DatasetGraph getDatasetGraph() {
//        return getDelegate().getDatasetGraph();
//    }

    @Override
    default void execute() {
        UpdateProcessor delegate = getDelegate();
        beforeExec();
        try {
            delegate.execute();
        } catch(Exception e) {
            onException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    /**
     * Gives a wrapper a change to perform an action before execution.
     * This method should only be called by {@link #execute()}
     * and never manually.
     */
    default void beforeExec() {
//        UpdateProcessor delegate = getDelegate();
//        if (delegate instanceof UpdateExecWrapper) {
//            ((UpdateExecWrapper)delegate).beforeExec();
//        }
    }

    /**
     * Gives a wrapper a change to perform an action after execution.
     * This method should only be called by {@link #execute()}
     * and never manually.
     */
    default void afterExec() {
//        UpdateProcessor delegate = getDelegate();
//        if (delegate instanceof UpdateExecWrapper) {
//            ((UpdateExecWrapper)delegate).afterExec();
//        }
    }

    /**
     * Gives a wrapper a change to perform an action in case of an execution
     * during execution.
     * This method should only be called by {@link #execute()}
     * and never manually.
     */
    default void onException(Exception e) {
//        UpdateProcessor delegate = getDelegate();
//        if (delegate instanceof UpdateExecWrapper) {
//            ((UpdateExecWrapper)delegate).onException(e);
//        }
    }
}
