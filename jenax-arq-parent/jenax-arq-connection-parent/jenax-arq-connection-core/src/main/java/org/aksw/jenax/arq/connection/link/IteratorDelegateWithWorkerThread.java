package org.aksw.jenax.arq.connection.link;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.PrefetchIterator;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;

public class IteratorDelegateWithWorkerThread<T, I extends Iterator<T>>
    extends PrefetchIterator<T>
    implements IteratorCloseable<T>
{
    protected WorkerThreadBase helper;
    protected int batchSize = 128;
    protected I delegate;

    public IteratorDelegateWithWorkerThread(I delegate, ExecutorService es) {
        super();
        this.helper = new WorkerThreadBase(es);
        this.delegate = delegate;
    }

    public I getDelegate() {
        return delegate;
    }

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

    @Override
    public void close() {
        helper.submit(() -> Iter.close(getDelegate()));
    }
}
