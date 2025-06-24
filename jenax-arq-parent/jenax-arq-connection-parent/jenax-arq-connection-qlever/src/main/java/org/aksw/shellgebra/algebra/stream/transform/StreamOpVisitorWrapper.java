package org.aksw.shellgebra.algebra.stream.transform;

import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;

public interface StreamOpVisitorWrapper<T>
    extends StreamOpVisitor<T>
{
    StreamOpVisitor<T> getDelegate();

    @Override
    default T visit(StreamOpFile op) {
        return getDelegate().visit(op);
    }

    @Override
    default T visit(StreamOpTranscode op) {
        return getDelegate().visit(op);
    }

    @Override
    default T visit(StreamOpConcat op) {
        return getDelegate().visit(op);
    }

    @Override
    default T visit(StreamOpCommand op) {
        return getDelegate().visit(op);
    }

    @Override
    default T visit(StreamOpVar op) {
        return getDelegate().visit(op);
    }
}
