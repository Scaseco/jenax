package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateProcessor;

public class UpdateExecWrapperTxn<T extends UpdateProcessor>
    extends UpdateExecWrapperBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public UpdateExecWrapperTxn(T decoratee, Transactional transactional) {
        super(decoratee);
        this.transactional = transactional;
    }

    @Override
    public void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(ReadWrite.WRITE);
        }
    }

    @Override
    public void onException(Exception e) {
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
    }


    public static <T extends UpdateProcessor> UpdateExec wrap(T decoratee, Transactional transactional) {
        return new UpdateExecWrapperTxn<>(decoratee, transactional);
    }

}
