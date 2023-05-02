package org.aksw.jenax.sparql.datasource.observable;

import java.util.concurrent.Future;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class CachingPublisher<T> {
    protected BehaviorProcessor<T> publisher;
    protected Flowable<T> flowable;
    protected Future<T> future;

    public CachingPublisher(BehaviorProcessor<T> publisher, Flowable<T> server) {
        super();
        this.publisher = publisher;
        this.flowable = server;
    }

    public BehaviorProcessor<T> getPublisher() {
        return publisher;
    }

    public Flowable<T> getFlowable() {
        return flowable;
    }
}