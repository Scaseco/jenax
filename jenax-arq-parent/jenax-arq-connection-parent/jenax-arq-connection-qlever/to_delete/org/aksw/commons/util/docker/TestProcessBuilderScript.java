package org.aksw.commons.util.docker;

import org.junit.Test;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.invocation.InvokableProcessBuilderHost;
import org.aksw.shellgebra.exec.invocation.ScriptContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessBuilderScript {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessBuilderScript.class);

    @Test
    public void test01() throws Exception {
        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);

            InvokableProcessBuilderHost pb = new InvokableProcessBuilderHost();
            pb.script("echo 'hello world'; echo 'it worked';", ScriptContent.contentTypeBash);
//	        pb.redirectInput(new JRedirectJava(Redirect.INHERIT));
//	        pb.redirectOutput(new JRedirectJava(Redirect.INHERIT));
//	        pb.redirectError(new JRedirectJava(Redirect.INHERIT));
            Process p = pb.start(runner);
            p.waitFor();
        }
    }
}
