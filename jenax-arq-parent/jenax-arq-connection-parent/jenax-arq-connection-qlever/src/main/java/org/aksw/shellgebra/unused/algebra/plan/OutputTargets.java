package org.aksw.shellgebra.unused.algebra.plan;

import java.nio.file.Path;
import java.util.Map;

/** Capture information about the output of a shell command. */
public class OutputTargets {
    protected boolean writesToStdout;
    protected Map<Path, ?> fileTargets;
}
