package org.aksw.jsheller.exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.jsheller.exec.FileWriterTaskBase.PathLifeCycle;

/**
 * Utils to handle creation and deletion of files. For use with FileWriterTask.
 */
public class PathLifeCycles {
    public static interface PathLifeCycleWrapper extends PathLifeCycle {
        PathLifeCycle getDelegate();

        @Override
        default void beforeExec(Path item) throws IOException {
            getDelegate().beforeExec(item);
        }

        @Override
        default void afterExec(Path item) throws IOException {
            getDelegate().afterExec(item);
        }
    }

    public static class PathLifeCycleWrapperBase
        implements PathLifeCycleWrapper {

        protected PathLifeCycle delegate;

        public PathLifeCycleWrapperBase(PathLifeCycle delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public PathLifeCycle getDelegate() {
            return delegate;
        }
    }

    public static class PathLifeCycleForNamedPipe implements PathLifeCycle {
        @Override
        public void beforeExec(Path path) throws IOException {
            SysRuntimeImpl.forCurrentOs().createNamedPipe(path);
        }

        @Override
        public String toString() {
            return "namedPipe";
        }
    }

    public static class PathLifeCycleDeleteAfterExec
        extends PathLifeCycleWrapperBase
    {
        public PathLifeCycleDeleteAfterExec(PathLifeCycle delegate) {
            super(delegate);
        }

        @Override
        public void afterExec(Path path) throws IOException {
            Files.deleteIfExists(path);
        }

        @Override
        public String toString() {
            return "deleteAfterExec/" + getDelegate();
        }
    }

    private static final PathLifeCycle NONE = new PathLifeCycle() { /* nothing here */ };

    public static PathLifeCycle none() {
        return NONE;
    }

    public static PathLifeCycle namedPipe() {
        return new PathLifeCycleForNamedPipe();
    }

    public static PathLifeCycle deleteAfterExec(PathLifeCycle delegate) {
        PathLifeCycle result = delegate instanceof PathLifeCycleDeleteAfterExec
            ? delegate
            : new PathLifeCycleDeleteAfterExec(delegate);
        return result;
    }
}
