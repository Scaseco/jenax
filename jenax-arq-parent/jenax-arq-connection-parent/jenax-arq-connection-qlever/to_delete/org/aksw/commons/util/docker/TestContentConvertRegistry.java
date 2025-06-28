package org.aksw.commons.util.docker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import org.aksw.jenax.shellgebra.cmd.ContentConvertRegistry;
import org.aksw.jenax.shellgebra.cmd.JvmCommandRapper;
import org.aksw.shellgebra.shim.cmd.JavaStreamTransform;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.apache.commons.io.IOUtils;

public class TestContentConvertRegistry {
    @Test
    public void test01() throws IOException {
        String ttlStr = """
        PREFIX eg: <http://www.example.org/>
        eg:s
          eg:p
            eg:o
              .
        """;

        String expectedStr = "<http://www.example.org/s> <http://www.example.org/p> <http://www.example.org/o> .\n";

        JavaStreamTransform xform = ContentConvertRegistry.get().getJavaConverter("ttl", "nt", null).get();

        String actualStr;
        try (InputStream in = xform.inputStreamTransform().apply(new ByteArrayInputStream(ttlStr.getBytes(StandardCharsets.UTF_8)))) {
            actualStr = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        Assert.assertEquals(expectedStr, actualStr);
    }

    @Test
    public void test02() throws IOException {
        String ttlStr = """
        PREFIX eg: <http://www.example.org/>
        eg:s
          eg:p
            eg:o
              .
        """;

        String expectedStr = "<http://www.example.org/s> <http://www.example.org/p> <http://www.example.org/o> .\n";

        JvmCommandRegistry registry = new JvmCommandRegistry();
        String cmdName = "/rapper";
        registry.put(cmdName,  new JvmCommandRapper());

        // JvmCommandRapper cmd = new JvmCommandRapper();
//         Stage stageBuilder = registry.newStage(cmdName, "-i", "ttl", "-o", "nt");
//        BoundStage stage = stageBuilder.from(ByteSource.wrap(ttlStr.getBytes()));
//        String actualStr = stage.toByteSource().asCharSource(StandardCharsets.UTF_8).read();
//        Assert.assertEquals(expectedStr, actualStr);

//        Tool tool = ContentConvertRegistry.get().getCmdConverter("ttl", "nt", null).get(0);
//        System.err.println(tool.name() + ": " + tool.argsBuilder().build());
//
//        ToolInfoImpl toolInfo = ToolRegistry.get().getToolInfo(tool.name()).orElse(null);
//        System.err.println(toolInfo.getCommandsByPath());
//        String actualStr;
//        try (InputStream in = xform.inputStreamTransform().apply(new ByteArrayInputStream(ttlStr.getBytes(StandardCharsets.UTF_8)))) {
//            actualStr = IOUtils.toString(in, StandardCharsets.UTF_8);
//        }
//
//        Assert.assertEquals(expectedStr, actualStr);
    }
}
