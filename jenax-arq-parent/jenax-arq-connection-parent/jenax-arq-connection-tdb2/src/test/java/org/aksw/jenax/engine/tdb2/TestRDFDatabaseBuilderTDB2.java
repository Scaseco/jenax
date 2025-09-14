package org.aksw.jenax.engine.tdb2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseBuilder;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.junit.Assert;
import org.junit.Test;

public class TestRDFDatabaseBuilderTDB2 {
    @Test
    public void test01() throws Exception {
        String testData = """
        <urn:s> <urn:p> <urn:o> .
        """;

        Path tmpDir = Files.createTempDirectory("test-store-tdb2");
        try {
            Path testDataFile = tmpDir.resolve("data.nt");
            Files.write(testDataFile, testData.getBytes(StandardCharsets.UTF_8));

            Path dbDir = Files.createDirectory(tmpDir.resolve("tdb2"));

            RDFEngineFactoryRegistry registry = RDFEngineFactoryRegistry.get();
            RDFDatabaseBuilder<?> dbBuilder = registry.getDatabaseFactory("tdb2").newBuilder();
            RDFDatabase db = dbBuilder
                .setOutputFolder(dbDir)
                .addPath(testDataFile.toString())
                .build();

            try (RDFEngine engine = registry.getEngineFactory("tdb2")
                .newBuilder().setDatabase(db).build()) {

                DatasetGraph dsg = engine.getLinkSource().getDatasetGraph();
                try (AutoTxn txn = Txn.autoTxn(dsg, TxnType.READ)) {
                    Table table = engine.getLinkSource().newQuery().query("SELECT * { ?s ?p ?o }").table();
                    Assert.assertEquals(1, table.size());
                    txn.commit();
                }
            }

            db.getFileSet().delete();
        } finally {
            try (Stream<Path> paths = Files.walk(tmpDir)) {
                paths.sorted(Comparator.reverseOrder()).map(Path::toFile)
                // .forEach(x -> System.out.println("Deleting: " + x));
                .forEach(File::delete);
            }
        }
    }
}
