package org.aksw.shellgebra.algebra.file.op;

public interface FileOpVisitor<T> {
    T visit(FileOpName op);
    T visit(FileOpTranscode op);
    T visit(FileOpVar op);
    T visit(FileOpOverStreamOp op);
}
