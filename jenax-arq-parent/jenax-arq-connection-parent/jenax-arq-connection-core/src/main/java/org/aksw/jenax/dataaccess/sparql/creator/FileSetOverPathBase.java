package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileSetOverPathBase
    implements FileSet
{
    private static final Logger logger = LoggerFactory.getLogger(FileSetOverPathBase.class);

    protected Path basePath;

    public FileSetOverPathBase(Path basePath) {
        super();
        this.basePath = Objects.requireNonNull(basePath);
        safetyCheck(basePath);
    }

    /** Prevent placing a database directly in the home or root directory. */
    public static void safetyCheck(Path path) {
        Path userPath = FileUtils.getUserDirectory().toPath();

        Path absPath = path.toAbsolutePath();
        if (absPath.getParent() == null) {
            throw new IllegalArgumentException("File set must not be the root path. Rejected: " + absPath);
        } else if (absPath.equals(userPath)) {
            throw new IllegalArgumentException("File set must not be the home directory. Rejected: " + absPath);
        }
    }

    public Path getBasePath() {
        return basePath;
    }

    /** Override that prevents deletion outside of the base path */
    @Override
    public void delete() throws IOException {
        List<Path> paths = getPaths();
        for (Path path : paths) {
            if (!path.startsWith(basePath)) {
                logger.warn("Prevented deletion outside of base folder: " + path + " is not a subfolder of " + basePath);
            } else {
                Files.delete(path);
            }
        }
    }

    @Override
    public String toString() {
        String sizeStr;

        try {
            sizeStr = Long.toString(byteSize());
        } catch (Throwable e) {
            sizeStr = "(failed to compute size: " + e + ")";
        }

        List<Path> paths = null;
        String errorStr = "";
        try {
            paths = getPaths();
        } catch (Throwable t) {
            errorStr = ", error: " + t;
        }

        if (paths == null) {
            paths = List.of();
        }
        List<Path> relPaths = paths.stream()
            .map(basePath::relativize)
            .toList();

        // for sparse files: physical size <= logical size
        return "" + paths.size() + " files, total logical size " +
            sizeStr + " bytes, location " + basePath + ", files " + relPaths + errorStr;
    }
}
