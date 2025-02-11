package org.aksw.jsheller.algebra.cmd.transform;

import java.util.Arrays;

public record CmdString(String[] cmd, String scriptString) {
    public CmdString(String[] cmd, String scriptString) {
        if (cmd != null && scriptString != null) {
            throw new IllegalAccessError("Arguments are mutually exclusive.");
        }
        this.cmd = cmd == null ? null : Arrays.copyOf(cmd, cmd.length);
        this.scriptString = scriptString;
    }

    public boolean isCmd() {
        return cmd != null;
    }

    public boolean isScriptString() {
        return scriptString != null;
    }

    public CmdString(String[] cmdParts) {
        this(cmdParts, null);
    }

    public CmdString(String scriptString) {
        this(null, scriptString);
    }
}
