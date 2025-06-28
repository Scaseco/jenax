package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.commons.util.obj.HasSelf;

public interface StreamOpVisitor<T>
    extends HasSelf<StreamOpVisitor<T>>
{
    T visit(StreamOpFile op);
    T visit(StreamOpTranscode op);
    T visit(StreamOpContentConvert op);
    T visit(StreamOpConcat op);
    T visit(StreamOpCommand op);
    T visit(StreamOpVar op);
}
