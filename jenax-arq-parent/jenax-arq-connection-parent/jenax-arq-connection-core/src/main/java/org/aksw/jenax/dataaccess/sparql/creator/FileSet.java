package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

/** A file set is a set of regular files and directories. */
public interface FileSet {

    List<Path> getPaths() throws IOException;

    /** Return sum of the file byte sizes. */
    default long size() throws IOException {
        List<Path> paths = getPaths();
        long result = 0;
        for (Path path : paths) {
            // try {
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    result += Files.size(path);
                }
//            } catch (IOException e) {
//                LoggerFactory.getLogger(FileSet.class).warn("Ignoring unexpected error.", e);
//            }
        }
        return result;
    }

    default boolean isEmpty() throws IOException {
        List<Path> paths = getPaths();
        return paths.isEmpty();
    }

    /** For safety reasons, delete must be implemented. */
    default void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

// Generic delete is unsafe - override restricts deletion to a base folder
//    default void delete() throws IOException {
//        List<Path> paths = getPaths();
//        for (Path path : paths) {
//            Files.delete(path);
//        }
//    }
}
