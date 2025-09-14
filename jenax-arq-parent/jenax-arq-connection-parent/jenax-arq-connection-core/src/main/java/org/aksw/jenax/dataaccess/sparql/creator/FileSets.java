package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSets {
    private static final Logger logger = LoggerFactory.getLogger(FileSets.class);

    public static void accumulateIfExists(Collection<Path> acc, Path path) {
        if (Files.exists(path)) {
            acc.add(path);
        }
    }

    public static void accumulateFlat(Collection<Path> acc, Path root, String globPattern) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, globPattern)) {
            stream.forEach(acc::add);
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }
    }

    public static void accumulateNested(Collection<Path> acc, Path root, String globPattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
              .filter(p -> {
                  Path rp = root.relativize(p);
                  boolean r = matcher.matches(rp);
                  return r;
              })
              .forEach(acc::add);
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
