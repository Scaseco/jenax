package org.aksw.commons.util.docker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import org.aksw.vshell.registry.DynamicInput;
import org.aksw.vshell.registry.DynamicInputFromStream;
import org.aksw.vshell.registry.DynamicOutput;
import org.aksw.vshell.registry.DynamicOutputFromStream;

import junit.framework.Assert;

public class TestDynamicIo {
    @Test
    public void test01() throws IOException {
        String expectedString = "hello world";
        byte[] expectedBytes = expectedString.getBytes();
        try (ByteArrayInputStream in = new ByteArrayInputStream(expectedBytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DynamicInput input = DynamicInputFromStream.of(in);
                DynamicOutput output = DynamicOutputFromStream.of(out)) {

            input.getFile();
            output.getFile();

            input.transferTo(output);
            output.flush();

            // Close ensures that all pending data in the pipe is written to the destination.
            // XXX Can we avoid explicit call to close to ensure that all data was written.
            output.close();

            byte[] actualBytes = out.toByteArray();
            String actualString = new String(actualBytes);
            Assert.assertEquals(expectedString, actualString);
        }
    }
}
