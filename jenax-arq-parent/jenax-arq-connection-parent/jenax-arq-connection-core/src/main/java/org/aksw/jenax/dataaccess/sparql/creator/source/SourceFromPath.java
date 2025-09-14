package org.aksw.jenax.dataaccess.sparql.creator.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

public class SourceFromPath
    implements Source
{
    private Path path;
    private OpenOption[] openOptions;

    public SourceFromPath(Path path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public String getName() {
        return path.toString();
    }

    @Override
    public InputStream open() throws IOException {
        return Files.newInputStream(path, openOptions);
    }

    @Override
    public String toString() {
        return getName();
    }
}
