package org.aksw.jenax.engine.qlever;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.creator.FileSetMatcher;
import org.aksw.jenax.dataaccess.sparql.creator.FileSetOverPathBase;
import org.aksw.jenax.dataaccess.sparql.creator.FileSetOverPathMatcher;
import org.aksw.jenax.dataaccess.sparql.creator.FileSets;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;

public class RdfDatabaseQlever
    implements RDFDatabase
{
    protected Path path;
    protected String indexName;
    protected FileSetOverPathBase fileSet;

    public RdfDatabaseQlever(Path path, String indexName) {
        super();
        this.path = Objects.requireNonNull(path);
        this.indexName = Objects.requireNonNull(indexName);
        this.fileSet = getFileSet(path, indexName);
    }

    public static FileSetOverPathBase getFileSet(Path path, String indexName) {
        return new FileSetOverPathMatcher(path, new FileSetMatcherQlever(indexName));
    }

    public Path getPath() {
        return path;
    }

    public String getIndexName() {
        return indexName;
    }

    @Override
    public FileSetOverPathBase getFileSet() {
        return fileSet;
    }

    @Override
    public String toString() {
        return "Qlever database " + fileSet.toString();
    }

    public static class FileSetMatcherQlever
        implements FileSetMatcher
    {
        protected String indexName;

        public FileSetMatcherQlever(String indexName) {
            super();
            this.indexName = Objects.requireNonNull(indexName);
        }

        public String getIndexName() {
            return indexName;
        }

        @Override
        public List<Path> match(Path path) {
            return assembleFileSet(path, indexName);
        }
    }

    public static List<Path> assembleFileSet(Path path, String indexName) {
        List<Path> result = new ArrayList<>();
        FileSets.accumulateIfExists(result, path.resolve(".stxxl"));
        // TODO Add .stxxl file
        FileSets.accumulate(result, path, indexName + ".*");
        return result;
    }
}
