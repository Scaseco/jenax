package org.aksw.jsheller.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Writer task based on a system process created from a system call. */
public class FileWriterTaskFromProcess extends FileWriterTaskViaExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FileWriterTaskFromProcess.class);

    private DefaultExecutor executor;
    private CommandLine cmdLine;

    public FileWriterTaskFromProcess(Path outputPath, PathLifeCycle pathLifeCycle, String... cmd) {
        super(outputPath, pathLifeCycle);

        String execCmdStr = cmd[0];
        String[] cmdArgs = Arrays.copyOfRange(cmd, 1, cmd.length);

        cmdLine = new CommandLine(execCmdStr);
        cmdLine.addArguments(cmdArgs, false);
    }

    protected final void beforeExec() throws IOException {
        pathLifeCycle.beforeExec(outputPath);
    }

    @Override
    protected void prepareWriteFile() throws IOException {
        LogOutputStream logOutputStream = new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                logger.warn(line);
            }
        };

        executor = DefaultExecutor.builder().get();
        executor.setStreamHandler(new PumpStreamHandler(logOutputStream, logOutputStream));

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT_DURATION).get();
        executor.setWatchdog(watchdog);
    }

    @Override
    public void runWriteFile() throws ExecuteException, IOException {
        executor.execute(cmdLine);
    }

    @Override
    public void abortActual() {
        // Super class takes care that this method is only called once and only after writeFile has been called.
        ExecuteWatchdog watchdog = executor.getWatchdog();
        if (watchdog.isWatching()) {
            logger.warn("Destroying process");
            watchdog.destroyProcess();
        }
    }

    @Override
    protected void onCompletion() throws IOException {
        // nothing to do
    }
}
