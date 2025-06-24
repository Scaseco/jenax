package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface SysRuntime {
    String which(String cmdName) throws IOException, InterruptedException;

    /** Quote a filename for use as an argument.*/
    String quoteFileArgument(String fileName);

    String[] compileCommand(CmdOp op);

    CmdStrOps getStrOps();

    /** Create a named pipe at the given path. */
    void createNamedPipe(Path path) throws IOException;

    /**
     * Resolve the first argument of the array against {@link #which(String)}.
     * Returned array is always a copy.
     */
    default String[] resolveCommand(String... cmd) throws IOException, InterruptedException {
        Objects.requireNonNull(cmd);
        if (cmd.length == 0) {
            throw new IllegalArgumentException("Command must not be an empty array.");
        }

        String cmdName = cmd[0];
        String resolvedName = which(cmdName);
        if (resolvedName == null) {
            throw new RuntimeException("Command not found: " + cmdName);
        }

        String[] result = Arrays.copyOf(cmd, cmd.length);
        result[0] = resolvedName;
        return result;
    }
}
