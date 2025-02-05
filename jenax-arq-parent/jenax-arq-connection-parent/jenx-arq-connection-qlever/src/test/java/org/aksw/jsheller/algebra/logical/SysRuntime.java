package org.aksw.jsheller.algebra.logical;

import java.io.IOException;

import org.aksw.jsheller.algebra.physical.CmdOp;

public interface SysRuntime {
    String which(String cmdName) throws IOException, InterruptedException;

    /** Quote a filename for use as an argument.*/
    String quoteFileArgument(String fileName);

    String[] compileCommand(CmdOp op);
}
