package org.aksw.jenax.dataaccess.sparql.common;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public interface TransactionalWrapper
    extends Transactional
{
    /** This method needs to be overridden for transaction support */
    Transactional getDelegate();

    /**
     * Overriding this method allows for providing handling transaction-related operations
     * separately from other aspects of the delegate.
     */
    default Transactional getTransactionalDelegate() {
        Transactional result = getDelegate();
        return result;
    }

    @Override
    default boolean isInTransaction() {
        Transactional delegate = getTransactionalDelegate();
        boolean result = delegate == null ? false : delegate.isInTransaction();
        return result;
    }

    @Override
    default void begin(ReadWrite readWrite) {
        Transactional delegate = getTransactionalDelegate();
        if (delegate != null) {
            delegate.begin(readWrite);
        }
    }

    @Override
    default void commit() {
        Transactional delegate = getTransactionalDelegate();
        if (delegate != null) {
            delegate.commit();
        }
    }

    @Override
    default void abort() {
        Transactional delegate = getTransactionalDelegate();
        if (delegate != null) {
            delegate.abort();
        }
    }

    @Override
    default void end() {
        Transactional delegate = getTransactionalDelegate();
        if (delegate != null) {
            delegate.end();
        }
    }

    @Override
    default void begin(TxnType type) {
        Transactional delegate = getTransactionalDelegate();
        if (delegate != null) {
            delegate.begin(type);
        }
    }

    @Override
    default boolean promote(Promote mode) {
        Transactional delegate = getTransactionalDelegate();
        boolean result = delegate == null ? false : delegate.promote(mode);
        return result;
    }

    @Override
    default ReadWrite transactionMode() {
        Transactional delegate = getTransactionalDelegate();
        ReadWrite result = delegate == null ? null : delegate.transactionMode();
        return result;
    }

    @Override
    default TxnType transactionType() {
        Transactional delegate = getTransactionalDelegate();
        TxnType result = delegate == null ? null : delegate.transactionType();
        return result;
    }
}
