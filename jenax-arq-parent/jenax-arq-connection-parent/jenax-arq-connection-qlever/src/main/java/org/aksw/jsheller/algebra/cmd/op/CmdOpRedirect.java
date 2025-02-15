package org.aksw.jsheller.algebra.cmd.op;

import java.util.Objects;

public class CmdOpRedirect
    extends CmdOp1
{
    protected String fileName;

    public CmdOpRedirect(String fileName, CmdOp subOp) {
        super(subOp);
        this.fileName = Objects.requireNonNull(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(redirect " + fileName + " " + subOp + ")";
    }
}
