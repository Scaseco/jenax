package org.aksw.jsheller.algebra.logical.op;

import java.util.Objects;

public class CodecOpFile
    extends CodecOp0
{
    protected String path;

    public CodecOpFile(String path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    public String getPath() {
        return path;
    }

    @Override
    public <T> T accept(CodecOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "CodecOpFile [pathStr=" + path + "]";
    }
}
