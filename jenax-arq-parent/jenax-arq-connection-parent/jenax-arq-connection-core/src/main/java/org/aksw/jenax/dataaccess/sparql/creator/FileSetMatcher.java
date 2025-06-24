package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileSetMatcher {
    List<Path> match(Path path) throws IOException;
}
