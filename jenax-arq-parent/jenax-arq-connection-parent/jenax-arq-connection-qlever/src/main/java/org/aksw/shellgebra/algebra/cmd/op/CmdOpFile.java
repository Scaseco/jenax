package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Objects;

public class CmdOpFile
    extends CmdOp0
{
    protected String path;

    public CmdOpFile(String path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "(file (" + path + "))";
    }
}
