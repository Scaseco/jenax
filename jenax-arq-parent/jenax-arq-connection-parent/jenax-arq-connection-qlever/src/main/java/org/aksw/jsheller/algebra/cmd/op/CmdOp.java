package org.aksw.jsheller.algebra.cmd.op;

public interface CmdOp {
    <T> T accept(CmdOpVisitor<T> visitor);
}
