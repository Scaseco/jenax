package org.aksw.commons.util.docker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.shim.cmd.JavaCodec;
import org.apache.commons.io.IOUtils;

public class TestCodecRegistry {

    @Test
    public void testJavaBzip2() throws IOException {
        assertRoundTrip("bzip2", "test content");
    }

    @Test
    public void testJavaGzip() throws IOException {
        assertRoundTrip("gz", "test content");
    }

    private static void assertRoundTrip(String codecName, String expected) throws IOException {
        String actual;
        JavaCodec codec = CodecRegistry.get().requireJavaCodec(codecName);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (OutputStream out = codec.encoder().outputStreamTransform().apply(baos)) {
                out.write(expected.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            try (InputStream in = codec.decoder().inputStreamTransform().apply(new ByteArrayInputStream(baos.toByteArray()))) {
                actual = IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        }

        Assert.assertEquals(expected, actual);
    }

//    @Test
//    public void testCmdBzip2() {
//        CodecVariant variant = CodecRegistry.get().getCodecSpec("bzip2").get().getDecoderVariants().get(0);
//        String toolName = variant.getToolName();
//        Assert.assertEquals("-cd", variant.getArgs().get(0));
//
//        ToolInfoImpl toolInfo = ToolRegistry.get().getToolInfo(toolName).get();
//        CommandTargetInfoImpl entry = toolInfo.getCommandsByPath().values().iterator().next();
//
//        Assert.assertTrue(entry.getDockerImages().contains("nestio/lbzip2"));
//    }
}
