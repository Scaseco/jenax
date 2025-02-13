package org.aksw.jsheller.exec;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public interface FileWriterTask
    extends AutoCloseable
{
    /** The path that will be written to by this writer. */
    Path getOutputPath();

    void start();
    void abort();
    void waitForCompletion() throws ExecutionException, InterruptedException;
}
