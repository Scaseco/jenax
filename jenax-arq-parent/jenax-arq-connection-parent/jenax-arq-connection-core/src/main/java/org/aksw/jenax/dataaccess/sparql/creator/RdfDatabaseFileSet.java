package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface RdfDatabaseFileSet {
    List<Path> getPaths();

    /** Return sum of the file byte sizes. */
    default long size() throws IOException {
        List<Path> paths = getPaths();
        long result = 0;
        for (Path path : paths) {
            result += Files.size(path);
        }
        return result;
    }
}
