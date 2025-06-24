package org.aksw.jenax.dataaccess.sparql.link.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.jenax.dataaccess.sparql.common.WorkerThreadBase;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;

public class IteratorDelegateWithWorkerThread<T, I extends Iterator<T>>
    extends PrefetchIterator<T>
    implements IteratorCloseable<T>
{
    protected WorkerThreadBase helper;

    /** Number of items to transfer in batch from the worker thread to the calling thread */
    protected int batchSize;
    protected I delegate;

    public IteratorDelegateWithWorkerThread(I delegate, ExecutorService es) {
        this(delegate, es, 128);
    }

    public IteratorDelegateWithWorkerThread(I delegate, ExecutorService es, int batchSize) {
        super();
        this.helper = new WorkerThreadBase(es);
        this.delegate = delegate;
        this.batchSize = batchSize;
    }

    public I getDelegate() {
        return delegate;
    }

    /** Certain objects such as TDB2 Bindings must be copied in order to detach them from
     * resources that are free'd when the iterator is closed.
     */
    protected T copy(T item) {
        return item;
    }

    @Override
    protected Iterator<T> prefetch() throws Exception {
        List<T> batch = helper.submit(() -> {
            I d = getDelegate();
            List<T> r = new ArrayList<>();
            for (int i = 0; i < batchSize && d.hasNext(); ++i) {
                T rawItem = d.next();
                T item = copy(rawItem);
                r.add(item);
            }
            return r;
        });
        return batch.isEmpty() ? null : batch.iterator();
    }

    // Note: The worker is blocked while retrieving so in that case any close signal won't get through
    @Override
    public void close() {
        helper.submit(() -> Iter.close(getDelegate()));
    }
}
