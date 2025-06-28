package org.aksw.commons.util.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class TestContainerUtils {

    /** Access a container's output via an input stream. */
    @Test
    public void testInputStream_01() throws IOException, Exception {

        String input = "'hello'\nworld\n";
        String actual;

        // Expected result has an extra trailing newline.
        String expected = input + "\n";

        String arg = input
            .replace("\"", "\\\"")
            .replace("\\n", "\\\\n");

        try (InputStream in = ContainerUtils.newInputStream(new GenericContainer<>("ubuntu:latest")
                .withCommand("sh", "-c", "echo \"" + arg + "\""))) {
            actual = IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        Assert.assertEquals(expected, actual);
    }

    /** Access a container's output via an input stream. */
    @Test
    public void testInputStream_02() throws IOException, Exception {

        List<String> expected = IntStream.range(1, 10).mapToObj(Integer::toString).toList();
        List<String> actual;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ContainerUtils.newInputStream(new GenericContainer<>("ubuntu:latest")
                .withCommand("bash", "-c", "for i in {1..100}; do echo $i; done")), StandardCharsets.UTF_8))) {
            actual = br.lines().limit(9).toList();
        }
        Assert.assertEquals(expected, actual);
    }
}
