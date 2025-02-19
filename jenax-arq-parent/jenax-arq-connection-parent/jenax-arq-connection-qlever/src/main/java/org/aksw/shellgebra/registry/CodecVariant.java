package org.aksw.shellgebra.registry;

import java.util.Arrays;

// Variants: Different commands / paths but same arguments.
public class CodecVariant {

    protected String[] cmd;

    private CodecVariant(String[] cmd) {
        super();
        this.cmd = cmd;
    }

    public static CodecVariant of(String ...cmd) {
        return new CodecVariant(cmd);
    }

    public String[] getCmd() {
        return Arrays.copyOf(cmd, cmd.length);
    }

//    CodecOpCommand createCmdFromFile(String file) {
//        return new CodecOpCommand(new String[] {"cat", file});
//    }

//    CodecOp createCmdFromSubCmd(SysRuntime env, String[] cmd) {
//        String  env.processSubstitute(cmd);
//        // return null;
//    }

}
