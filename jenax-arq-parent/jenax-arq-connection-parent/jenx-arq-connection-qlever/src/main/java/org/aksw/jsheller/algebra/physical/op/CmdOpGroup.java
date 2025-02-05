package org.aksw.jsheller.algebra.physical.op;

import java.util.List;

public class CmdOpGroup
    extends CmdOpN
{
    public CmdOpGroup(List<CmdOp> subOps) {
        super(subOps);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
