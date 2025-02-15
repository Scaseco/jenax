package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSets {
    private static final Logger logger = LoggerFactory.getLogger(FileSets.class);

    public static void accumulateIfExists(Collection<Path> acc, Path path) {
        if (Files.exists(path)) {
            acc.add(path);
        }
    }

    public static void accumulate(Collection<Path> acc, Path path, String globPattern) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, globPattern)) {
            stream.forEach(acc::add);
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }
    }

    /** List files and directories in an order safe for deletion. */
    public static void listPathsDepthFirst(Collection<Path> acc, Path rootDir) throws IOException {
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                acc.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                acc.add(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                logger.warn("Failed to access file: " + file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
