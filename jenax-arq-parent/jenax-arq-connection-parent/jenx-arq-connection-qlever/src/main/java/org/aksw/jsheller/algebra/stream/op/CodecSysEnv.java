package org.aksw.jsheller.algebra.stream.op;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jsheller.exec.SysRuntime;

// Runtime + Cache of resolved commands
// XXX Perhaps better implemented as a CachingSysRuntime.
public class CodecSysEnv {
    private SysRuntime runtime;

    // Command names that were resolved to paths - typically using which
    private Map<String, String> resolvedCommands = new HashMap<>();

    public CodecSysEnv(SysRuntime runtime) {
        super();
        this.runtime = runtime;
    }

    public SysRuntime getRuntime() {
        return runtime;
    }

    public void setResolve(String cmd, String resolution) {
        resolvedCommands.put(cmd, resolution);
    }
}
