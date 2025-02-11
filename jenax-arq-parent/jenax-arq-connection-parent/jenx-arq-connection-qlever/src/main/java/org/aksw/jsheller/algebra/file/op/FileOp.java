package org.aksw.jsheller.algebra.file.op;

public interface FileOp {
    <T> T accept(FileOpVisitor<T> visitor);
}
