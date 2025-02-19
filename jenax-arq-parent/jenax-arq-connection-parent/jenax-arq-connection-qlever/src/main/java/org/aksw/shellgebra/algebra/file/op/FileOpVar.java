package org.aksw.shellgebra.algebra.file.op;

import java.util.Objects;

public class FileOpVar
    extends FileOp0
{
    protected String varName;

    public FileOpVar(String varName) {
        super();
        this.varName = Objects.requireNonNull(varName);
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public <T> T accept(FileOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
