package org.aksw.jenax.arq.service.vfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemMgr {

    private static class RefCount<T> {
        final T value;
        int count;

        RefCount(T value) {
            this.value = value;
            this.count = 1; // Starts with 1 owner
        }
    }

    private static final Map<URI, RefCount<FileSystem>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * Acquires a FileSystem instance. Increments the reference count.
     */
    public static FileSystem acquire(URI uri, Map<String, ?> env) throws IOException {
        RefCount<FileSystem> refCount = REGISTRY.compute(uri, (k, v) -> {
            if (v != null) {
                v.count++;
            } else {
                FileSystem fs = getOrCreateFileSystem(uri, env);
                v = new RefCount<>(fs);
            }
            return v;
        });
        return refCount.value;
    }

    private static FileSystem getOrCreateFileSystem(URI uri, Map<String, ?> env) {
        // Try to get existing from JVM cache, or create new
        FileSystem fs;
        try {
            fs = FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            try {
                try {
                    fs = FileSystems.newFileSystem(uri, env);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            } catch (FileSystemAlreadyExistsException concurrentCreation) {
                fs = FileSystems.getFileSystem(uri);
            }
        }
        return fs;
    }

    /**
     * Releases a FileSystem instance. Decrements the reference count.
     * Closes the FileSystem if no references remain.
     */
    public static void release(URI uri) {
        REGISTRY.compute(uri, (k, rc) -> {
            if (rc != null) {
                rc.count--;

                if (rc.count <= 0) {
                    if (rc.value.isOpen()) {
                        try {
                            rc.value.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
            }
            return rc;
        });
    }
}
