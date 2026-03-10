package org.aksw.commons.util.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import org.aksw.shellgebra.io.pipe.NamedPipe;

public class TestPipes {
    // FIXME This is more an experiment rather than a test.
    /**
     * Example with two competing readers on a pipe.
     * Data is immediately delivered to the buffer of any open reader.
     * This means, that even if a reader never calls read() it will still have data delivered to it.
     * @throws InterruptedException
     */
    // @Test
    public void test01() throws IOException, InterruptedException {
        Path path = Path.of("my-pipe-" + System.nanoTime());
        NamedPipe.create(path);
        Set<Integer> collector = Collections.newSetFromMap(new ConcurrentHashMap<>());

        int n = 100000;
        try {
            Thread t1 = readerThread(() -> Files.newInputStream(path), "in1: ", collector);
            Thread t2 = readerThread(() -> Files.newInputStream(path), "in2: ", collector);

            t1.start();
            t2.start();

            InputStream noReadReader = null;
            // Opening the output stream won't block because of the already opened reader threads.
            try (PrintStream out = new PrintStream(Files.newOutputStream(path), false, StandardCharsets.UTF_8)) {
                // noReadReader = Files.newInputStream(path);
                for (int i = 0; i < n; ++i) {
                    out.println("x" + i);
                }
                out.flush();
            }

            t1.join();
            t2.join();

            System.out.println("Data delivered to no read reader:");
            if (noReadReader != null) {
                noReadReader.transferTo(System.out);
                noReadReader.close();
            }
            System.out.println("Seen numbers: " + collector.size());

            Set<Integer> missingNumbers = new LinkedHashSet<>();
            for (int i = 0; i < n; ++i) {
                if (!collector.contains(i)) {
                    missingNumbers.add(i);
                }
            }
            System.out.println("Missing: " + missingNumbers.size());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    /** A reader that does not read is held while a thread consumes data. */
    @Test
    public void testHeldReader() throws IOException, InterruptedException {
        Path path = Path.of("my-pipe-" + System.nanoTime());
        NamedPipe.create(path);
        Set<Integer> collector = Collections.newSetFromMap(new ConcurrentHashMap<>());

        InputStream[] noReadReaderTmp = {null};
        Thread thread = new Thread(() -> {
            try {
                noReadReaderTmp[0] = Files.newInputStream(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        int n = 100000;
        try {
            Thread t1 = readerThread(() -> Files.newInputStream(path), "in1: ", collector);
            // Thread t2 = readerThread(() -> Files.newInputStream(path), "in2: ", collector);

            // t2.start();

            // Opening the output stream won't block because of the already opened reader threads.
            InputStream noReadReader = null;
            thread.start();
            try (PrintStream out = new PrintStream(Files.newOutputStream(path), false, StandardCharsets.UTF_8)) {
                thread.join();
                noReadReader = noReadReaderTmp[0];

                t1.start();

                // noReadReader = Files.newInputStream(path);
                for (int i = 0; i < n; ++i) {
                    out.println("x" + i);
                }
                out.flush();
            }

            t1.join();
            // t2.join();

            System.out.println("Data delivered to no read reader:");
            if (noReadReader != null) {
                noReadReader.transferTo(System.out);
                noReadReader.close();
            }
            System.out.println("Seen numbers: " + collector.size());

            Set<Integer> missingNumbers = new LinkedHashSet<>();
            for (int i = 0; i < n; ++i) {
                if (!collector.contains(i)) {
                    missingNumbers.add(i);
                }
            }
            System.out.println("Missing: " + missingNumbers.size());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    public static Thread readerThread(Callable<InputStream> in, String prefix, Set<Integer> collector) {
        return new Thread(() -> {
            System.out.println("Reader thread created - prefix=" + prefix);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in.call(), StandardCharsets.UTF_8))) {
                br.lines().forEach(line -> {
                    // Every line is supposed to start with 'x'.
                    boolean isValidLine = line.startsWith("x") && !line.substring(1).contains("x");
                    if (!isValidLine) {
                        System.out.println("Line mismatch: " + line);
                    } else {
                        String numStr = line.replaceAll("^x", "");
                        Integer value = Integer.parseInt(numStr);
                        collector.add(value);
                        // System.out.println(prefix + numStr);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                System.out.println("Reader thread terminated - prefix=" + prefix);
            }
        });
    }
}
