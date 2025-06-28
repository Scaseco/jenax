package org.aksw.jenax.engine.qlever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.input.ProxyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    public static InputStream exec(String cmd) throws IOException, InterruptedException {
        return exec(new ProcessBuilder(cmd));
    }

    public static InputStream exec(String ...cmd) throws IOException, InterruptedException {
        return exec(new ProcessBuilder(cmd));
    }

    public static Process run(Consumer<String> logger, String ...cmd) throws IOException, InterruptedException {
        return run(new ProcessBuilder(cmd), logger);
    }

    // FIXME If the process exists fast then the reader thread still doesn't get all messages
    public static Process run(ProcessBuilder processBuilder, Consumer<String> logger) throws IOException, InterruptedException {
        Process process = processBuilder
            .redirectErrorStream(true)
            .start();

        InputStream in = process.getInputStream();
        Thread outputReaderThread = new StreamReaderThread(process, in);
        outputReaderThread.start();
        return process;
    }

    public static void runAndWait(Consumer<String> logger, String ...cmd) throws IOException {
        try {
            Process p = run(logger, cmd);
            int exitValue = p.waitFor();
            failIfNonZero(exitValue);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class StreamReaderThread extends Thread {
        // private static final Logger logger = LoggerFactory.getLogger(StreamReaderThread.class);

        protected Process process;
        protected InputStream in;
        protected Consumer<String> lineConsumer;
        protected AtomicBoolean isTerminating;

        public StreamReaderThread(Process process, InputStream in) {
            this(process, in, new AtomicBoolean(), logger::info);
        }

        public StreamReaderThread(Process process, InputStream in, AtomicBoolean isTerminating, Consumer<String> lineHandler) {
            super();
            this.process = Objects.requireNonNull(process);
            this.in = Objects.requireNonNull(in);
            this.isTerminating = Objects.requireNonNull(isTerminating);
            this.lineConsumer = Objects.requireNonNull(lineHandler);

        }

        @Override
        public void run() {
            try (BufferedReader read = new BufferedReader(new InputStreamReader(in))) {
                read.lines().takeWhile(x -> !isInterrupted()).forEach(line -> lineConsumer.accept(line));
            } catch (Exception e) {
                handleException(e);
            }
        }

        protected void handleException(Exception e) {
            if (process.isAlive() && !isTerminating.get()) {
                throw new RuntimeException(e);
            } else {
                // Ignore errors during termination
            }
        }
    }

    public static void failIfNonZero(Process process) {
        int exitValue = process.exitValue();
        failIfNonZero(exitValue);
    }

    public static void failIfNonZero(int exitValue) {
        if (exitValue != 0) {
            // TODO Check for non-zero exit value should also be made before read!
            throw new RuntimeException("Process exited with non-zero code: " + exitValue);
        }
    }

    public static InputStream exec(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();

        AtomicBoolean isTerminating = new AtomicBoolean(false);
        int terminationWaitTimeInSeconds = 5;

        Thread errorReaderThread = new StreamReaderThread(process, process.getErrorStream(), isTerminating, logger::info);
        errorReaderThread.start();

        InputStream core = process.getInputStream();
        InputStream result = new ProxyInputStream(core) {
            @Override
            public void close() throws IOException {
                int exitCode;
                if (!process.isAlive()) {
                    failIfNonZero(process);
                } else {
                    isTerminating.set(true);

                    process.destroy();

                    try { process.waitFor(terminationWaitTimeInSeconds, TimeUnit.SECONDS); } catch (InterruptedException e) {}

                    if (process.isAlive()) {
                        process.destroyForcibly();
                    }

                    try { process.waitFor(terminationWaitTimeInSeconds, TimeUnit.SECONDS); } catch (InterruptedException e) {}

                    if (process.isAlive()) {
                        throw new RuntimeException("Failed to terminate process.");
                    }

                    try {
                        errorReaderThread.join();
                    } catch (InterruptedException e) {
                        // Ignore
                    } finally {
                        super.close();
                    }
                }
            }
        };
        return result;
    }

    public static String getCommandOutput(String... command) throws IOException {
        try {
            InputStream in = exec(command);
            String result;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
            return result;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static String which(String commandName) throws IOException {
        return getCommandOutput("which", commandName);
    }

    public static int getUID() throws IOException {
        return Integer.parseInt(getCommandOutput("id", "-u"));
    }

    public static int getGID() throws IOException {
        return Integer.parseInt(getCommandOutput("id", "-g"));
    }
}
