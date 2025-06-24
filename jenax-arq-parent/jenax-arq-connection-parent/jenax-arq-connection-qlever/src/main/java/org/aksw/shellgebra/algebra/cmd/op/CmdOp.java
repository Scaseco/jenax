package org.aksw.shellgebra.algebra.cmd.op;

public interface CmdOp {
    <T> T accept(CmdOpVisitor<T> visitor);
}
