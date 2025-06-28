package org.aksw.commons.util.docker;

import org.junit.Assert;
import org.junit.Test;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;

public class TestCmdOpToString {
    @Test
    public void testBashPipe() {
        CmdOp cmdOp = CmdOpPipeline.of(
            CmdOpExec.of("/my/tool.sh", CmdArg.ofLiteral("-f"), CmdArg.ofPathString("/foo/bar.dat")),
            CmdOpExec.of("/my/conv.sh", CmdArg.ofLiteral("-convert")));

        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdString cmdString = runtime.compileString(cmdOp);
        String actualScriptString = cmdString.scriptString();

        String expectedScriptString = "/my/tool.sh -f /foo/bar.dat | /my/conv.sh -convert";
        Assert.assertEquals(expectedScriptString, actualScriptString);

//        List<String> expected = List.of("/usr/bin/bash", "-c", "/my/tool.sh -f /foo/bar.dat | /my/conv.sh -convert");
//        List<String> actual = List.of(SysRuntimeImpl.forCurrentOs().compileCommand(cmdOp));
//        Assert.assertEquals(expected, actual);
    }
}
