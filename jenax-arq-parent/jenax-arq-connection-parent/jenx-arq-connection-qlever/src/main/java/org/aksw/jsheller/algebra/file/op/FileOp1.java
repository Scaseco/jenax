package org.aksw.jsheller.algebra.file.op;

import java.util.Objects;

public abstract class FileOp1
    extends FileOpBase
{
    protected final FileOp subOp;

    public FileOp1(FileOp subOp) {
        super();
        this.subOp = Objects.requireNonNull(subOp);
    }

    public FileOp getSubOp() {
        return subOp;
    }
}
