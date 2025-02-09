package org.aksw.jenax.engine.qlever;

import java.nio.file.Path;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabase;
import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFileSet;

public class RdfDatabaseQlever
    implements RdfDatabase
{
    protected Path path;
    protected String indexName;

    protected RdfDatabaseFileSet fileSet;

    public RdfDatabaseQlever(Path path, String indexName) {
        super();
        this.path = path;
        this.indexName = indexName;

        this.fileSet = new RdfDatabaseFileSetQlever(path, indexName);
    }

    public Path getPath() {
        return path;
    }

    public String getIndexName() {
        return indexName;
    }

    @Override
    public RdfDatabaseFileSet getFileSet() {
        return fileSet;
    }
}
