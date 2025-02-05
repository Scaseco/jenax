package org.aksw.jsheller.exec;

import java.io.IOException;

import org.aksw.jsheller.algebra.physical.op.CmdOp;

public interface SysRuntime {
    String which(String cmdName) throws IOException, InterruptedException;

    /** Quote a filename for use as an argument.*/
    String quoteFileArgument(String fileName);

    String[] compileCommand(CmdOp op);
}
