package org.aksw.jsheller.algebra.stream.transformer;

import java.util.List;

import org.aksw.jsheller.algebra.stream.op.HasStreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;

public interface StreamOpTransformGeneric<T extends HasStreamOp> {
    T transform(StreamOpFile op);
    T transform(StreamOpTranscode op, T subOp);
    T transform(StreamOpConcat op, List<T> subOps);
    T transform(StreamOpCommand op);
    T transform(StreamOpVar op);
}
