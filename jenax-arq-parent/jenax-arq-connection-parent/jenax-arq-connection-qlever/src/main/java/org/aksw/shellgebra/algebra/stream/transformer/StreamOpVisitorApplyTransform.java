package org.aksw.shellgebra.algebra.stream.transformer;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.stream.op.HasStreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;

public class StreamOpVisitorApplyTransform<T extends HasStreamOp>
    implements StreamOpVisitor<T>
{
    protected StreamOpTransformGeneric<T> transform;

    public StreamOpVisitorApplyTransform(StreamOpTransformGeneric<T> transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public T visit(StreamOpFile op) {
        T result = transform.transform(op);
        return result;
    }

    @Override
    public T visit(StreamOpTranscode op) {
        StreamOp subOp = op.getSubOp().getStreamOp();
        T newSubOp = subOp.accept(this);
        T result = transform.transform(op, newSubOp);
        return result;
    }

    @Override
    public T visit(StreamOpContentConvert op) {
        StreamOp subOp = op.getSubOp().getStreamOp();
        T newSubOp = subOp.accept(this);
        T result = transform.transform(op, newSubOp);
        return result;
    }

    @Override
    public T visit(StreamOpConcat op) {
        List<T> newSubOps = op.getSubOps().stream()
            .map(HasStreamOp::getStreamOp)
            .map(subOp -> subOp.accept(this)).toList();
        T result = transform.transform(op, newSubOps);
        return result;
    }

    @Override
    public T visit(StreamOpCommand op) {
        T result = transform.transform(op);
        return result;
    }

    @Override
    public T visit(StreamOpVar op) {
        T result = transform.transform(op);
        return result;
    }
}
