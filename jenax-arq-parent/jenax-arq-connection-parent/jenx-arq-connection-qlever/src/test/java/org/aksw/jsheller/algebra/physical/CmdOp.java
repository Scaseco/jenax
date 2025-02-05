package org.aksw.jsheller.algebra.physical;

public interface CmdOp {
    <T> T accept(CmdOpVisitor<T> visitor);
}
