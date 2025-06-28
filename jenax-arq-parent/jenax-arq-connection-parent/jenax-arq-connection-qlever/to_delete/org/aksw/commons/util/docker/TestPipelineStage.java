package org.aksw.commons.util.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteSource;

public class TestPipelineStage {
    /**
     * Pipeline that (1) prints a text using printf on the host,
     * (2) bzip2-encodes using java and (3) bzip2-decodes using docker.
     */
    @Test
    public void test01() throws IOException {
        FileMapper fileMapper = FileMapper.of("/shared");

        String expected = "hello";
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", expected);
        Stage inputStage = Stages.host(cmdOp);

        Stage pipelineStage = Stages.pipeline(
            // Stages.javaOut(BZip2CompressorOutputStream::new),
            // Stages.javaIn(BZip2CompressorInputStream::new),
            Stages.javaOut(BZip2CompressorOutputStream::new),
            Stages.docker("nestio/lbzip2", CmdOpExec.ofLiterals("/usr/bin/lbzip2", "-d"), fileMapper, null)
        );

        ByteSource bs = pipelineStage.from(inputStage.fromNull()).toByteSource();

        String actual = bs.asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);

        // System.out.println("Base value: " + base.forNullInput().toByteSource().asCharSource(StandardCharsets.UTF_8).read());
    }
}
