package org.aksw.jenax.sparql.datasource.observable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

/**
 * A map where values are flowables that are created dynamically for a given key
 * When the last subscriber unsubscribes from a flowable then the key is automatically removed.
 */
public class ObservableSourceImpl<K, V>
    implements ObservableSource<K, V>
{
    protected Map<K, CachingPublisher<V>> queryToPublisher;
    protected Function<? super K, ? extends V> computeFn;

    public ObservableSourceImpl(Function<? super K, ? extends V> computeFn) {
        super();
        this.queryToPublisher = new ConcurrentHashMap<>();
        this.computeFn = computeFn;
    }

    // @Override
    public Flowable<V> observe(K key) {

        CachingPublisher<V> e = queryToPublisher.computeIfAbsent(key, k -> {

            // https://stackoverflow.com/questions/41021978/rxjava2-publishprocessor-callbacks-for-the-first-subscribed-and-the-last-unsubs
            AtomicInteger counter = new AtomicInteger();
            BehaviorProcessor<V> pp = BehaviorProcessor.create();

            Flowable<V> f = pp
                .doOnSubscribe(s -> {
                    if (counter.getAndIncrement() == 0) {
                        try {
                            V value = computeFn.apply(key);
                            pp.onNext(value);
                        } catch (Exception t) {
                            pp.onError(t);
                        }
                    }
                })
                .doOnCancel(() -> {
                    // Cancel a running future
                })
                .doFinally(() -> {
                    // Check within compute() which locks on the key. This prevents race conditions
                    queryToPublisher.compute(key, (kk, vv) -> {
                        CachingPublisher<V> r = counter.decrementAndGet() == 0 ? null : vv;
                        if (r == null) {
                            // vv.getFuture().cance<l();
                        }

                        return r;
                    });
                });


            return new CachingPublisher<V>(pp, f);
        });

        return e.getFlowable();
    }

    @Override
    public void refreshAll(boolean cancelRunning) {
        for (Entry<K, CachingPublisher<V>> e : queryToPublisher.entrySet()) {
            K key = e.getKey();
            BehaviorProcessor<V> publisher = e.getValue().getPublisher();
            V oldValue = publisher.getValue();
            V newValue = computeFn.apply(key);
            if (!Objects.equals(oldValue, newValue)) {
                publisher.onNext(newValue);
            }
        }
    }
}
