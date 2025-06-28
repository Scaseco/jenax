package org.aksw.commons.util.docker;

import java.io.IOException;

import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeFactoryDocker;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import junit.framework.Assert;


public class TestSysRuntimeDocker {
    // @Test
    public void test00() {
        try (GenericContainer<?> container = new GenericContainer<>("ubuntu:24.04")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh"))
                .withCommand("-c", "which sh")
                ) {

            container.start();
            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
            System.out.println(exitCode);
            // return exitCode;
        }
    }

    @Test
    public void test01() throws IOException, InterruptedException {
        SysRuntimeFactoryDocker f = SysRuntimeFactoryDocker.create();
        String str;
        try (SysRuntime sys = f.create("ubuntu:24.04")) {
            str = sys.which("cat");
            // System.out.println(sys.exists("/foo/bar"));
        }
        Assert.assertEquals("/usr/bin/cat", str);
    }

    @Test
    public void test02() throws IOException, InterruptedException {
        SysRuntimeFactoryDocker f = SysRuntimeFactoryDocker.create();
        boolean b;
        try (SysRuntime sys = f.create("ubuntu:24.04")) {
            b = sys.exists("/usr/bin/cat");
        }
        Assert.assertTrue(b);
    }
}
