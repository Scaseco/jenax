package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;

public class UpdateExecDecoratorTxn<T extends UpdateExec>
    extends UpdateExecDecoratorBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public UpdateExecDecoratorTxn(T decoratee, Transactional transactional) {
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
    }


    public static <T extends UpdateExec> UpdateExec wrap(T decoratee, Transactional transactional) {
        return new UpdateExecDecoratorTxn<>(decoratee, transactional);
    }

}
