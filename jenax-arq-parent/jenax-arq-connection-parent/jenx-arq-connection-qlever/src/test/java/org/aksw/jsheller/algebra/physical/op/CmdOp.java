package org.aksw.jsheller.algebra.physical.op;

public interface CmdOp {
    <T> T accept(CmdOpVisitor<T> visitor);
}
