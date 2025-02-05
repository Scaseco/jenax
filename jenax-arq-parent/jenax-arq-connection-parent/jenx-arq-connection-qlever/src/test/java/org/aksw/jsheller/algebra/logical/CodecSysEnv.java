package org.aksw.jsheller.algebra.logical;

import java.util.HashMap;
import java.util.Map;

// Runtime + Cache of resolved commands
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
