package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class FileSetOverPathList
    implements FileSet
{
    protected List<Path> paths;

    public FileSetOverPathList(List<Path> paths) {
        super();
        this.paths = Objects.requireNonNull(paths);
    }

    @Override
    public List<Path> getPaths() {
        return paths;
    }

    @Override
    public void delete() throws IOException {
        List<Path> paths = getPaths();
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
