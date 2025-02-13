package org.aksw.jsheller.algebra.stream.transformer;

import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.jsheller.algebra.stream.op.HasStreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOp;

/** Helper record structure for use with {@link StreamOpTransformGeneric}. */
public record StreamOpEntry<T>(StreamOp op, T data)
    implements HasStreamOp, Entry<StreamOp, T>
{
    public StreamOpEntry(StreamOp op, T data) {
        this.op = Objects.requireNonNull(op);
        this.data = data;
    }

    @Override
    public StreamOp getStreamOp() {
        return op;
    }

    @Override
    public StreamOp getKey() {
        return op;
    }

    @Override
    public T getValue() {
        return data;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }
}
