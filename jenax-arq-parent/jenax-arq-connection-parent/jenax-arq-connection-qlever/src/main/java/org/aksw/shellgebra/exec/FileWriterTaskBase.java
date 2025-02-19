package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FileWriterTaskBase
    implements FileWriterTask
{
    public enum TaskState {
        NEW,       // Not started
        STARTING,  // In transition to running; task submitted to executor (if applicable).
        RUNNING,   // Writing in progress; executor has started the runnable
        COMPLETED, // Finished successfully
        FAILED,    // Error occurred
        ABORTED    // Stopped before completion
    }

    public static interface PathLifeCycle {
        default void beforeExec(Path path) throws IOException {}
        default void afterExec(Path path) throws IOException {}
    }

    /**  The file being generated. */
    protected Path outputPath;
    protected PathLifeCycle pathLifeCycle;

    protected AtomicReference<TaskState> state = new AtomicReference<>(TaskState.NEW);
    protected volatile boolean isAbortCalled = false;

//    public FileWriterTask(Path path) {
//        this(path, PathLifeCycles.none());
//    }

    public FileWriterTaskBase(Path path, PathLifeCycle pathLifeCycle) {
        super();
        this.outputPath = Objects.requireNonNull(path);
        this.pathLifeCycle = Objects.requireNonNull(pathLifeCycle);
    }

    public Path getOutputPath() {
        return outputPath;
    }

    protected void checkIfNew() {
        if (state.get() != TaskState.NEW) {
            throw new IllegalStateException("File writing can only start once.");
        }
    }

    /** Only call this method from within a synchronized block! */
    protected void checkIfAbortHasBeenCalled() {
        if (isAbortCalled) {
            throw new RuntimeException("Task aborted.");
        }
    }

    public boolean isStarted() {
        return state.get() == TaskState.RUNNING;
    }

    public boolean isFinished() {
        return state.get() == TaskState.COMPLETED || state.get() == TaskState.FAILED || state.get() == TaskState.ABORTED;
    }

    public TaskState getState() {
        return state.get();
    }

    /** Method that allows for setting up resources in an atomic way. STARTING. */
    protected abstract void prepareWriteFile() throws IOException;

    protected abstract void runWriteFile() throws IOException;

    /** Called on completion - regardless whether successful or not. Only called if prepareWriteFile has been called before. (doWriteFile may not have been called). */
    protected abstract void onCompletion() throws IOException;

    public abstract void waitForCompletion() throws ExecutionException, InterruptedException;

    public abstract void abort();
}
