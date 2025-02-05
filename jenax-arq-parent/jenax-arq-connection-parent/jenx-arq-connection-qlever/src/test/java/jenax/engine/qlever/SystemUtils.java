package jenax.engine.qlever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public static InputStream exec(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();

        AtomicBoolean isTerminating = new AtomicBoolean(false);
        int terminationWaitTimeInSeconds = 5;

        Thread errorReaderThread = new Thread() {
            @Override
            public void run() {
                try (BufferedReader read = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    read.lines().takeWhile(x -> !isInterrupted()).forEach(line -> logger.info(line));
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
        };

        errorReaderThread.start();

        InputStream core = process.getInputStream();
        InputStream result = new ProxyInputStream(core) {
            @Override
            public void close() throws IOException {
                int exitCode;
                if (!process.isAlive()) {
                    exitCode = process.exitValue();
                    if (exitCode != 0) {
                        throw new RuntimeException("Process exited with non-zero code: " + exitCode);
                    }
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

    public static String getCommandOutput(String... command) throws IOException, InterruptedException {
        InputStream in = exec(command);
        String result;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return result;
    }

    public static String which(String commandName) throws IOException, InterruptedException {
        return getCommandOutput("which", commandName);
    }

    public static int getUID() throws IOException, NumberFormatException, InterruptedException {
        return Integer.parseInt(getCommandOutput("id", "-u"));
    }

    public static int getGID() throws IOException, NumberFormatException, InterruptedException {
        return Integer.parseInt(getCommandOutput("id", "-g"));
    }
}
