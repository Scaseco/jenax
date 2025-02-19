package org.aksw.shellgebra.exec;

/** Resolve a command to a path. The command may be an alias or an absolute or relative path. */
public interface CmdResolver {
    String resolve(String path);
}
