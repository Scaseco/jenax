package org.aksw.jenax.dataaccess.sparql.common;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public abstract class TransactionalWrapperWithWorkerThread<T extends Transactional>
    extends WorkerThreadBase
    implements Transactional
{
    public abstract T getDelegate();

    @Override
    public void begin(TxnType type) {
        submit(() -> getDelegate().begin(type));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        submit(() -> getDelegate().begin(readWrite));
    }

    @Override
    public boolean promote(Promote mode) {
        return submit(() -> getDelegate().promote(mode));
    }

    @Override
    public void commit() {
        submit(() -> getDelegate().commit());
    }

    @Override
    public void abort() {
        submit(() -> getDelegate().abort());
    }

    @Override
    public void end() {
        submit(() -> getDelegate().end());
    }

    @Override
    public ReadWrite transactionMode() {
        return submit(() -> getDelegate().transactionMode());
    }

    @Override
    public TxnType transactionType() {
        return submit(() -> getDelegate().transactionType());
    }

    @Override
    public boolean isInTransaction() {
        return submit(() -> getDelegate().isInTransaction());
    }
}
