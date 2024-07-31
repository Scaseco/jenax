package org.aksw.jenax.graphql.sparql.v2.io;

import java.util.Objects;

public class ObjectNotationWriterInMemoryWrapper<T, K, V>
    implements ObjectNotationWriterWrapper<K, V>, ObjectNotationWriterInMemory<T, K, V>
{
    protected ObjectNotationWriter<K, V> delegate;
    protected ObjectNotationWriterInMemory<T, ?, ?> inMemorySink;

    public ObjectNotationWriterInMemoryWrapper(ObjectNotationWriter<K, V> delegate, ObjectNotationWriterInMemory<T, ?, ?> inMemorySink) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.inMemorySink = Objects.requireNonNull(inMemorySink);
    }

    @Override
    public T getProduct() {
        return inMemorySink.getProduct();
    }

    @Override
    public void clear() {
        inMemorySink.clear();
    }

    @Override
    public ObjectNotationWriter<K, V> getDelegate() {
        return delegate;
    }
}
