package org.aksw.jsheller.exec;

import java.util.concurrent.ExecutionException;

public interface FileWriterTask
    extends AutoCloseable
{
    void start();
    void abort();
    void waitForCompletion() throws ExecutionException, InterruptedException;
}
