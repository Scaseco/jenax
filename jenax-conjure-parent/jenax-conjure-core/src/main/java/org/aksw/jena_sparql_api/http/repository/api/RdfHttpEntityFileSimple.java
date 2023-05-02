package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.jena.rdf.model.Resource;

public class RdfHttpEntityFileSimple
    implements RdfHttpEntityFile
{
    protected Path path;
    protected Resource metaData;

    public RdfHttpEntityFileSimple(Path path, Resource metaData) {
        super();
        this.path = path;
        this.metaData = metaData;
    }

    @Override
    public Resource getCombinedInfo() {
        return metaData;
    }

    @Override
    public InputStream open() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getRelativePath() {
        return path;
    }

    @Override
    public Path getAbsolutePath() {
        return path;
    }

    @Override
    public RdfHttpResourceFile getResource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInfo(Consumer<? super Resource> consumer) {
        throw new UnsupportedOperationException();
    }
}
