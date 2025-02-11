package org.aksw.jsheller.algebra.stream.op;

import java.util.Objects;

public class StreamOpFile
    extends StreamOp0
{
    protected String path;

    public StreamOpFile(String path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    public String getPath() {
        return path;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(file (" + path + "))";
    }
}
