package org.aksw.jenax.dataaccess.sparql.execution.update;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorWrapperTxn<T extends UpdateProcessor>
    extends UpdateProcessorWrapperBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public UpdateProcessorWrapperTxn(T decoratee, Transactional transactional) {
        super(decoratee);
        this.transactional = transactional;
    }

    @Override
    protected void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(ReadWrite.WRITE);
        }
    }

    @Override
    protected void onException(Exception e) {
        seenThrowable = e;
        super.onException(e);
    }

    @Override
    public void afterExec() {
        if (startedTxnHere) {
            try {
                if (seenThrowable == null) {
                    transactional.commit();
                } else {
                    transactional.abort();
                }
            } finally {
                transactional.end();
            }
        }

        super.afterExec();
    }


    public static <T extends UpdateProcessor> UpdateProcessor wrap(T decoratee, Transactional transactional) {
        return new UpdateProcessorWrapperTxn<>(decoratee, transactional);
    }

}
