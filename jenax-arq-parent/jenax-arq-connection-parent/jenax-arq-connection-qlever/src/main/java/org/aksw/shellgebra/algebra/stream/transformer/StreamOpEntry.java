package org.aksw.shellgebra.algebra.stream.transformer;

import java.util.Map.Entry;

import org.aksw.shellgebra.algebra.stream.op.HasStreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;

import java.util.Objects;

/**
 * Groups a StreamOp with a custom value.
 * Because it implements {@link HasStreamOp} it can be used
 * directly with {@link StreamOpTransformGeneric}.
 */
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
