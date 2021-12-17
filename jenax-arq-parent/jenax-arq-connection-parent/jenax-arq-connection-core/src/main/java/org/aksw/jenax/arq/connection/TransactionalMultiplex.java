package org.aksw.jenax.arq.connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public class TransactionalMultiplex<T extends Transactional>
    implements Transactional
{
    protected Collection<? extends T> delegates;

    @SafeVarargs
    public TransactionalMultiplex(T ... delegates) {
        this(Arrays.asList(delegates));
    }

    public TransactionalMultiplex(Collection<? extends T> delegates) {
        super();
        this.delegates = delegates;
    }

    protected void forEach(Consumer<? super T> handler) {
        MultiplexUtils.forEach(delegates, handler);
    }

    protected <X> X forEachR(Function<? super T, X> handler) {
        return MultiplexUtils.forEachAndReturnFirst(delegates, handler);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        forEach(d -> d.begin(readWrite));
    }

    @Override
    public void commit() {
        forEach(Transactional::commit);
    }

    @Override
    public void abort() {
        forEach(Transactional::abort);
    }

    @Override
    public void end() {
        forEach(Transactional::end);
    }

    @Override
    public boolean isInTransaction() {
        boolean result = delegates.isEmpty() ? false : delegates.iterator().next().isInTransaction();
        return result;
    }

    @Override
    public void begin(TxnType type) {
        forEach(d -> d.begin(type));
    }

    @Override
    public boolean promote(Promote mode) {
        return forEachR(d -> d.promote(mode));
    }

    @Override
    public ReadWrite transactionMode() {
        return forEachR(Transactional::transactionMode);
    }

    @Override
    public TxnType transactionType() {
        return forEachR(Transactional::transactionType);
    }
}
