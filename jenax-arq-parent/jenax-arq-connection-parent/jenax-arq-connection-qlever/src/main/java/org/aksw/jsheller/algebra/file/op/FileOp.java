package org.aksw.jsheller.algebra.file.op;

// This hierarchy is still under revision. Probably all FileOps - with the exception of FileOpVar -
// need support for specifying the output filename.
public interface FileOp {
    <T> T accept(FileOpVisitor<T> visitor);
}
