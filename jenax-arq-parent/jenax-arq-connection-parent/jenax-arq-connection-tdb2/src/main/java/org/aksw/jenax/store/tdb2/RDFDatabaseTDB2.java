package org.aksw.jenax.store.tdb2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.creator.FileSetMatcher;
import org.aksw.jenax.dataaccess.sparql.creator.FileSetOverPathBase;
import org.aksw.jenax.dataaccess.sparql.creator.FileSetOverPathMatcher;
import org.aksw.jenax.dataaccess.sparql.creator.FileSets;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;

public class RDFDatabaseTDB2
    implements RDFDatabase
{
    protected Path path;
    // protected String baseName;
    protected FileSetOverPathBase fileSet;

    /** Path to a TDB2 database, without the Data-0001 folders. */
    public RDFDatabaseTDB2(Path path) {
        super();
        this.path = Objects.requireNonNull(path);
        this.fileSet = getFileSet(path);
    }

    public static FileSetOverPathBase getFileSet(Path path) {
        return new FileSetOverPathMatcher(path, new FileSetMatcherTDB2());
    }

    public Path getPath() {
        return path;
    }

    @Override
    public FileSetOverPathBase getFileSet() {
        return fileSet;
    }

    @Override
    public String toString() {
        return "TDB2 database " + fileSet.toString();
    }

    public static class FileSetMatcherTDB2
        implements FileSetMatcher
    {
        public FileSetMatcherTDB2() {
            super();
        }

        @Override
        public List<Path> match(Path path) {
            return assembleFileSet(path);
        }
    }

    public static List<Path> assembleFileSet(Path path) {
        List<Path> result = new ArrayList<>();
        try {
            FileSets.accumulateNested(result, path, "glob:Data-*/**");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
