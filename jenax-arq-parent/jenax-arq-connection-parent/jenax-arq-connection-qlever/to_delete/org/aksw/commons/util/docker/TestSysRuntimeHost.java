package org.aksw.commons.util.docker;

import java.io.IOException;

import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.SysRuntimeWrapperShellEnv;
import org.aksw.shellgebra.exec.shell.ShellEnv;
import org.junit.Test;

public class TestSysRuntimeHost {
    @Test
    public void test01() throws IOException, InterruptedException {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        ShellEnv shellEnv = new ShellEnv();
        shellEnv.getEnv().put("PATH", "/home/raven/tmp");

        runtime = new SysRuntimeWrapperShellEnv(runtime, shellEnv);
        String str = runtime.which("foo.bar");
        System.out.println("Got: " + str);
    }
}
