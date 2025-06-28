package org.aksw.shellgebra.unused.algebra.plan;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface CommandShim {
    // Return a command that performs the operation on the given environment.
    CmdOp contentConvert(String srcFormat, String tgtFormat);
}
