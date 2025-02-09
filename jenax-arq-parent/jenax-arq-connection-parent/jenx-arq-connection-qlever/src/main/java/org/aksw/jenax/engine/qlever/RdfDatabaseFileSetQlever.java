package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFileSet;

public class RdfDatabaseFileSetQlever
    implements RdfDatabaseFileSet
{
    protected Path path;
    protected String indexName;

    public RdfDatabaseFileSetQlever(Path path, String indexName) {
        super();
        this.path = path;
        this.indexName = indexName;
    }

    @Override
    public List<Path> getPaths() {
        return assembleFileSet(path, indexName);
    }

    public static List<Path> assembleFileSet(Path path, String indexName) {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, indexName + ".*")) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
