package org.aksw.jenax.sparql.datasource.observable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class ObservableSourceTransformValue<K, V, W>
    implements ObservableSource<K, W>
{
    protected ObservableSource<K, V> delegate;
    protected FlowableTransformer<V, W> transformer;

    public ObservableSourceTransformValue(ObservableSource<K, V> delegate, FlowableTransformer<V, W> transformer) {
        super();
        this.delegate = delegate;
        this.transformer = transformer;
    }

    public ObservableSource<K, V> getDelegate() {
        return delegate;
    }

    @Override
    public Flowable<W> observe(K key) {
        Flowable<V> base = getDelegate().observe(key);
        Flowable<W> result = Flowable.fromPublisher(transformer.apply(base));
        return result;
    }

    @Override
    public void refreshAll(boolean cancelRunning) {
        getDelegate().refreshAll(cancelRunning);
    }
}
