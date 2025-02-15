package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class FileSetOverPathMatcher
    extends FileSetOverPathBase
{
    protected FileSetMatcher matcher;

    public FileSetOverPathMatcher(Path basePath, FileSetMatcher matcher) {
        super(basePath);
        this.matcher = Objects.requireNonNull(matcher);
    }

    public FileSetMatcher getMatcher() {
        return matcher;
    }

    @Override
    public List<Path> getPaths() throws IOException {
        Path path = getBasePath();
        List<Path> result = matcher.match(path);
        return result;
    }
}
