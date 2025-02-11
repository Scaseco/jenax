package org.aksw.jsheller.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/** A file writer task that does nothing (it just 'serves' an existing file). */
public class FileWriterTaskNoop
    extends FileWriterTaskBase
{
    public FileWriterTaskNoop(Path path, PathLifeCycle pathLifeCycle) {
        super(path, pathLifeCycle);
    }

    @Override
    public void start() {
        checkIfAbortHasBeenCalled();
        checkIfNew();
        state.set(TaskState.COMPLETED);
    }

    @Override
    public void abort() {
        state.compareAndSet(TaskState.NEW, TaskState.ABORTED);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    protected void prepareWriteFile() throws IOException {
    }

    @Override
    protected void runWriteFile() throws IOException {
    }

    @Override
    protected void onCompletion() throws IOException {
    }

    @Override
    public void waitForCompletion() throws ExecutionException, InterruptedException {
    }
}
