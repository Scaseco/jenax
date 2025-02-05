package org.aksw.jsheller.exec;

import java.io.IOException;

import org.aksw.jsheller.algebra.physical.op.CmdOp;

public interface SysRuntimeWrapper<X extends SysRuntime>
    extends SysRuntime
{
    X getDelegate();

    @Override
    default String which(String cmdName) throws IOException, InterruptedException {
        return getDelegate().which(cmdName);
    }

    @Override
    default String[] compileCommand(CmdOp op) {
        return getDelegate().compileCommand(op);
    }

    @Override
    default String quoteFileArgument(String fileName) {
        return getDelegate().quoteFileArgument(fileName);
    }
}
