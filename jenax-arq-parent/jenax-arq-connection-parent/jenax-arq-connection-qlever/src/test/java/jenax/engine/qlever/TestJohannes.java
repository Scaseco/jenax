package jenax.engine.qlever;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.aksw.jenax.dataaccess.sparql.creator.FileSet;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.engine.qlever.RDFDatabaseBuilderQlever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJohannes {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void test() throws IOException, InterruptedException {



        Path basePath = Path.of("src/test/resources").resolve("dbpedia-test-01").toAbsolutePath();
        logger.info("Loading files from: " + basePath);
        RDFDatabaseBuilderQlever<?> builder = new RDFDatabaseBuilderQlever<>();
        List<Path> filesToLoad = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, "*.ttl.gz")) {
            for (Path entry : stream) {
                filesToLoad.add(entry);
            }
        } catch (IOException | DirectoryIteratorException e) {
            logger.error("Failed to scan directory for datasets to load with qlever.", e);
        }
        int fileCount = filesToLoad.size();
        assertNotEquals("Unexpectedly did not find any files to load.", 0, fileCount);

        logger.info("Preparing to load {} files: {}", fileCount, filesToLoad);
        for (Path file : filesToLoad) {
            builder.addPath(file.toString());
        }

        Path outPath = Files.createTempDirectory("jenax-qlever-test");
        logger.info("Created temporary directory: {}", outPath);
        try {
            Files.createDirectories(outPath);
            builder.setOutputFolder(outPath);
            builder.setIndexName("foo");
            // Clean up on success - otherwise leave files for inspection.
            RDFDatabase db = builder.build();
            FileSet fileSet = db.getFileSet();
            int fileSetSize = fileSet.getPaths().size();
            logger.info("Qlever database fileset size: " + fileSetSize);

            //db.getFileSet().delete();
            fileSet.delete();
        } finally {
            Files.deleteIfExists(outPath);
        }
    }
}
