package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecDecoratorTxn<T extends QueryExec>
    extends QueryExecDecoratorBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public QueryExecDecoratorTxn(T decoratee, Transactional transactional) {
        super(decoratee);
        this.transactional = transactional;
    }

    @Override
    public void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(ReadWrite.READ);
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


    public static <T extends QueryExec> QueryExec wrap(T decoratee, Transactional transactional) {
        return new QueryExecDecoratorTxn<>(decoratee, transactional);
    }
}
