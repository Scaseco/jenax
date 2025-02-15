package org.aksw.jsheller.algebra.cmd.op;

public class CmdOpPipe
    extends CmdOp2
{
    public CmdOpPipe(CmdOp subOp1, CmdOp subOp2) {
        super(subOp1, subOp2);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(pipe " + getSubOp1() + " " + getSubOp2() + ")";
    }
}
