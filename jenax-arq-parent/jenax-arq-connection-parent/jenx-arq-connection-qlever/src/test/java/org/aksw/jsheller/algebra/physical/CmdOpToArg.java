package org.aksw.jsheller.algebra.physical;

// Operator to transform a sub op into an argument string
public class CmdOpToArg
    extends CmdOp1
{
    public CmdOpToArg(CmdOp subOp) {
        super(subOp);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
