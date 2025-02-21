package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.common.io.MoreFiles;

/** Adapter to the existing infrastructure - may become deprecated. */
public abstract class RDFEngineFactoryLegacyBase
    implements RDFEngineFactory
{
    private static final Logger logger = LoggerFactory.getLogger(RDFEngineFactoryLegacyBase.class);

    @Override
    public RDFEngineBuilder<?> newEngineBuilder() {
        return new RdfDataEngineBuilderBase() {
            @Override
            public RDFEngine build() throws Exception {
                // XXX Clash with RdfDataSourceSpecBasicFromMap.create()
                return RDFEngineFactoryLegacyBase.this.create(map);
            }
        };
    }

    @Override
    public abstract RDFEngine create(Map<String, Object> config) throws Exception;

    /**
     * The deteleOnClose flag tells whether calling close will attempt to delete the folder
     */
    public static record CloseablePath(Path path, Closeable closeable, boolean deleteOnClose) {}

    public static CloseablePath setupPath(String tempDirPrefix, RdfDataSourceSpecBasic spec) throws IOException {
        return setupPath(spec.getLocation(), spec.getLocationContext(),
            spec.getTempDir(), tempDirPrefix, spec.isAutoDeleteIfCreated());
    }

    public static CloseablePath setupPath(
            String location, String locationContext,
            String tempDir, String tempDirPrefix, boolean isAutoDeleteIfCreated) throws IOException {
        CloseablePath fsInfo = resolveFileSystemAndPath(locationContext, location);
        Path dbPath = fsInfo.path();
        Closeable fsCloseAction = fsInfo.closeable();

        CloseablePath info;
        try {
            info = createFolder(dbPath, tempDir, tempDirPrefix, isAutoDeleteIfCreated);
        } catch (Throwable e) {
            try {
                fsCloseAction.close();
            } finally {
                throw new RuntimeException(e);
            }
        }
        Path finalDbPath = info.path();
        Closeable folderDeleteAction = info.closeable();

        Closeable partialCloseAction = () -> {
            try {
                folderDeleteAction.close();
            } finally {
                fsCloseAction.close();
            }
        };

        return new CloseablePath(finalDbPath, partialCloseAction, info.deleteOnClose());
    }

    /**
     * Create a folder, possibly on a different filesystem. Also sets up a close action
     * that removes the folder if it was created by this call.
     * @throws IOException
     */
    public static CloseablePath createFolder(
            Path dbPath,
            String tempDir,
            String tempDirPrefix,
            boolean isAutoDeleteIfCreated) throws IOException {
//        Path dbPath = fsInfo == null ? null : fsInfo.getKey();
//        Closeable fsCloseAction = fsInfo == null ? () -> {} : fsInfo.getValue();

        boolean createdDbDir = false;
        if (dbPath == null) {
            String tmpDirStr = tempDir;
            if (tmpDirStr == null) {
                tmpDirStr = StandardSystemProperty.JAVA_IO_TMPDIR.value();
            }

            if (tmpDirStr == null) {
                throw new IllegalStateException("Temp dir neither specified nor obtainable from java.io.tmpdir");
            }

            Path tmpDir = Path.of(tmpDirStr);
            dbPath = Files.createTempDirectory(tmpDir, tempDirPrefix).toAbsolutePath();
            createdDbDir = true;
        } else {
            dbPath = dbPath.toAbsolutePath();
            if (!Files.exists(dbPath)) {
                // Parent directory must exist
                Files.createDirectory(dbPath);
//            	Path parentDir = dbPath.getParent();
//            	if (!Files.exists(parentDir)) {
//            		throw new RuntimeException("Parent directory of " + dbPath + " does not exist.");
//            	}

                // Files.createDirectories(dbPath);
                createdDbDir = true;
            }
        }

        Path finalDbPath = dbPath;
        Closeable deleteAction;
        CloseablePath result;
        if (createdDbDir) {
            if (Boolean.TRUE.equals(isAutoDeleteIfCreated)) {
                logger.info("Created new directory (its content will deleted when done): " + finalDbPath);
                deleteAction = () -> {
                    logger.info("Deleting created directory: " + finalDbPath);
                    Files.deleteIfExists(finalDbPath);
                    // MoreFiles.deleteRecursively(finalDbPath);
                    // Files.deleteIfExists(finalDbPath);
                };
                result = new CloseablePath(finalDbPath, deleteAction, true);
            } else {
                logger.info("Created new directory (will be kept after done): " + finalDbPath);
                deleteAction = () -> {};
                result = new CloseablePath(finalDbPath, deleteAction, false);
            }
        } else {
            logger.info("Folder already existed - delete action disabled: " + finalDbPath);
            deleteAction = () -> {};
            result = new CloseablePath(finalDbPath, deleteAction, false);
        }

        return result;
    }

    /**
     * Create a path on a (possibly remote) file system via Java nio.
     *
     * @param fsUri The url to a file system. Null or blank for the local one.
     * @param pathStr A path on the file system.
     * @return A pair comprising the path and a close action which closes the underlying file system.
     * @throws IOException
     */
    public static CloseablePath resolveFileSystemAndPath(String fsUri, String pathStr) throws IOException {
        Path dbPath = null;

        FileSystem fs;
        Closeable fsCloseActionTmp;
        if (fsUri != null && !fsUri.isBlank()) {
            fs = FileSystems.newFileSystem(URI.create(fsUri), Collections.emptyMap());
            fsCloseActionTmp = () -> fs.close();
        } else {
            fs = FileSystems.getDefault();
            fsCloseActionTmp = () -> {}; // noop
        }

        Closeable closeAction = fsCloseActionTmp;

        try {
            if (pathStr != null && !pathStr.isBlank()) {
                dbPath = fs.getPath(pathStr).toAbsolutePath();
//                for (Path root : fs.getRootDirectories()) {
//                    dbPath = root.resolve(pathStr);
//                    // Only consider the first root (if any)
//                    break;
//                }
            }
        } catch (Exception e) {
            try {
                closeAction.close();
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        }

        return new CloseablePath(dbPath, closeAction, false);
    }
}
