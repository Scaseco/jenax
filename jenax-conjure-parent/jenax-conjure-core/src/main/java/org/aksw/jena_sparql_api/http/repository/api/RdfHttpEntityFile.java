package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.jena.rdf.model.Resource;

public interface RdfHttpEntityFile
    extends RdfHttpEntity
{
    /** The link to the resource that owns this entity */
    RdfHttpResourceFile getResource();

    // Path relative to the resource
    Path getRelativePath(); // TODO Inherit from some file-based entity class

    default Path getAbsolutePath() {
        Path relPath = getRelativePath();
        Path parentAbsPath = getResource().getAbsolutePath();
        Path result = parentAbsPath.resolve(relPath);

        return result;
    }

    void updateInfo(Consumer<? super Resource> consumer);
}
