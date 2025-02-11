package org.aksw.jsheller.algebra.cmd.op;

public interface CmdOpVisitor<T> {
    T visit(CmdOpExec op);
    T visit(CmdOpPipe op);
    T visit(CmdOpGroup op);
    T visit(CmdOpString op);
    T visit(CmdOpSubst op);
    T visit(CmdOpToArg op);
    T visit(CmdOpFile op);
}
