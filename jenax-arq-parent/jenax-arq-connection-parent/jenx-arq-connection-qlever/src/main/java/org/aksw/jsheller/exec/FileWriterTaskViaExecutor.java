package org.aksw.jsheller.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class FileWriterTaskViaExecutor
    extends FileWriterTaskBase
{
    public FileWriterTaskViaExecutor(Path path, PathLifeCycle pathLifeCycle) {
        super(path, pathLifeCycle);
    }

    protected Object lock = new Object();
    private ExecutorService executor;
    private Future<?> futureTask;

    // Running state is reached once writeFile is called

    @Override
    public final void start() {
        synchronized (lock) {
            checkIfAbortHasBeenCalled();
            checkIfNew();

            state.set(TaskState.STARTING);
            executor = Executors.newSingleThreadExecutor();
            futureTask = executor.submit(new Worker());
        }
    }

    protected class Worker implements Runnable {
        boolean lifeCycleStarted = false;

        @Override
        public void run() {
            try {
                runInternal();
            } finally {
                cleanup();
            }
        }

        protected void cleanup() {
            if (lifeCycleStarted) {
                try {
                    pathLifeCycle.afterExec(outputPath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // if
                }
            }
        }

        protected void runInternal() {
            // Run preparation in a synchronized block to prevent concurrent abort
            synchronized (lock) {
                checkIfAbortHasBeenCalled();
                state.set(TaskState.RUNNING);
                try {
                    lifeCycleStarted = true;
                    pathLifeCycle.beforeExec(outputPath);
                    prepareWriteFile();
                } catch (Exception e) {
                    // We do not allow abort during prepareWriteFile
                    state.set(TaskState.FAILED);
                    throw new RuntimeException(e);
                }
            }

            try {
                checkIfAbortHasBeenCalled();
                runWriteFile();
                state.compareAndSet(TaskState.RUNNING, TaskState.COMPLETED);
            } catch (IOException e) {
                state.compareAndSet(TaskState.RUNNING, TaskState.FAILED);
                throw new CompletionException(e);
            } finally {
                try {
                    synchronized (lock) {
                        onCompletion();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void waitForCompletion() throws InterruptedException, ExecutionException {
        if (state.get() == TaskState.NEW) {
            throw new IllegalStateException("Task has not been started.");
        }

        if (futureTask != null) {
            futureTask.get();
        }
    }

    /**
     * Requests abort of currently running task or prevents its execution if it hasn't started yet.
     * Aborting a completed task has no effect.
     */
    @Override
    public final void abort() {
        synchronized (lock) {
            if (!isAbortCalled) {
                // Set a new task directly to aborted
                state.compareAndSet(TaskState.NEW, TaskState.ABORTED);

                isAbortCalled = true;
                boolean wasRunning = state.get() == TaskState.RUNNING;
                if (wasRunning) {
                    try {
                        try {
                            abortActual();
                        } finally {
                            if (!futureTask.isDone()) {
                                futureTask.cancel(true);
                            }
                        }
                    } finally {
                        state.compareAndSet(TaskState.RUNNING, TaskState.ABORTED);
                    }
                }
            }
        }
    }

    protected abstract void abortActual();

    @Override
    protected void onCompletion() throws IOException {
    }

    @Override
    public final void close() throws Exception {
        try {
            abort();
            waitForCompletion();
        } finally {
            try {
                closeActual();
            } finally {
                if (executor != null) {
                    executor.shutdown();
                }
            }
        }
    }
    protected void closeActual() throws Exception { }

    @Override
    public String toString() {
        return "(executorFileWriter " + getOutputPath() + "/" + pathLifeCycle + ")";
    }
}
