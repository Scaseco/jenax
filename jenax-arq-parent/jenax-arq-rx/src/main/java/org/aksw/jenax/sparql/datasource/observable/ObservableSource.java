package org.aksw.jenax.sparql.datasource.observable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

public interface ObservableSource<K, V> {
    Flowable<V> observe(K key);

    void refreshAll(boolean cancelRunning);

    default <W> ObservableSource<K, W> transformValues(FlowableTransformer<V, W> transformer) {
        return new ObservableSourceTransformValue<>(this, transformer);
    }
}
