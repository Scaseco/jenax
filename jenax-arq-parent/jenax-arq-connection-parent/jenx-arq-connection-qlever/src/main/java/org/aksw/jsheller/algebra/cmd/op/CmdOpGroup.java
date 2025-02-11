package org.aksw.jsheller.algebra.cmd.op;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return "(group " + subOps.stream().map(Object::toString).collect(Collectors.joining(" ")) + ")";
    }
}
