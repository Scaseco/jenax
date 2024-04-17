package org.aksw.jenax.dataaccess.sparql.link.dataset;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.sparql.core.Transactional;

/** Wrapper that automatically starts a transaction if there is no active one on the link */
public class LinkDatasetGraphWrapperTxn<T extends LinkDatasetGraph>
    extends LinkDatasetGraphWrapperBase<T>
{
    protected Transactional transactional;
    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public LinkDatasetGraphWrapperTxn(T delegate, Transactional transactional) {
        super(delegate);
        this.transactional = transactional;
    }

    @Override
    public Transactional getTransactionalDelegate() {
        return transactional;
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
    public void close() {
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
        super.close();
    }

    public static <T extends LinkDatasetGraph> LinkDatasetGraph wrap(T decoratee, Transactional transactional) {
        return new LinkDatasetGraphWrapperTxn<>(decoratee, transactional);
    }
}
