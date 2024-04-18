package org.aksw.jenax.dataaccess.sparql.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public class TransactionalMultiplex<T extends Transactional>
    implements Transactional
{
    protected List<? extends T> delegates;

    @SafeVarargs
    public TransactionalMultiplex(T ... delegates) {
        this(Arrays.asList(delegates));
    }

    public TransactionalMultiplex(List<? extends T> delegates) {
        super();
        this.delegates = delegates;
    }

    protected void forEach(Consumer<? super T> handler) {
        forEachR(delegate -> { handler.accept(delegate); return null; });
    }

    /**
     * This method may be overridden in order to lock the set of delegates.
     *
     * @param <X>
     * @param handler
     * @return
     */
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
        new ReverseListIterator<>(delegates).forEachRemaining(Transactional::end);
    }

    @Override
    public boolean isInTransaction() {
        Transactional firstDelegate = delegates.isEmpty()
                ? null
                : delegates.iterator().next();
        boolean result = firstDelegate == null ? false : firstDelegate.isInTransaction();
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
