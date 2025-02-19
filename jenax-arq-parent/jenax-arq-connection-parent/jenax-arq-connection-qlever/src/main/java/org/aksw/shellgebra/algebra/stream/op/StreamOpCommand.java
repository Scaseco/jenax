package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

/** Generate a stream from the output of a command */
// XXX Perhaps STDOUT / STDERR / COMBINED constants should be added.
public class StreamOpCommand
    extends StreamOp0
{
    protected CmdOp cmdOp;

    public StreamOpCommand(CmdOp cmdOp) {
        super();
        this.cmdOp = cmdOp;
    }

    public CmdOp getCmdOp() {
        return cmdOp;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(cmd " + cmdOp + ")";
    }
}
