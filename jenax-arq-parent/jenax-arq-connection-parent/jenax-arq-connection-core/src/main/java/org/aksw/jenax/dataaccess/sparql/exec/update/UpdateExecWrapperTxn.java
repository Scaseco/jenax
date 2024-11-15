package org.aksw.jenax.dataaccess.sparql.exec.update;

import java.util.Objects;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateProcessor;

public class UpdateExecWrapperTxn<T extends UpdateProcessor>
    extends UpdateExecWrapperBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;
    protected TxnType txnType;

    /** Note: Default txnType changed from WRITE to READ_PROMOTE with jenax 5.3.0 in order to allow for use
     * concurrent processing inside of update statements using Jena's Service Enhancer plugin.
     * INSERT { ... } WHERE { SERVICE <collect:concurrent:> { ... } }.
     */
    public UpdateExecWrapperTxn(T decoratee, Transactional transactional) {
        this(decoratee, transactional, TxnType.READ_PROMOTE);
    }

    public UpdateExecWrapperTxn(T decoratee, Transactional transactional, TxnType txnType) {
        super(decoratee);
        this.transactional = transactional;
        this.txnType = Objects.requireNonNull(txnType);
    }

    @Override
    public void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(txnType);
            // transactional.begin(ReadWrite.WRITE);
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
