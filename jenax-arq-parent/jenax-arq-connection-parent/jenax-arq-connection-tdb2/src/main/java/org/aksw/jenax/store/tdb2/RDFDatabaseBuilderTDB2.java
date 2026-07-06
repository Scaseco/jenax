package org.aksw.jenax.store.tdb2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFDatabaseBuilderTDB2<X extends RDFDatabaseBuilderTDB2<X>>
    extends RDFDatabaseBuilderBase<X>
{
    private static final Logger logger = LoggerFactory.getLogger(RDFDatabaseBuilderTDB2.class);

    @Override
    protected List<Lang> getSupportedLangs() {
        return Collections.unmodifiableList(Arrays.asList(Lang.TURTLE, Lang.NQUADS, Lang.RDFXML, Lang.RDFTHRIFT));
    }

    @Override
    public RDFDatabase build() throws IOException, InterruptedException {

        Path outputPath = outputFolder;

        List<UpdateLoad> workloads = new ArrayList<>();
        for (FileArg arg : args) {
            Node graph = arg.graph();
            Path path = arg.path();
            String pathStr = path.toAbsolutePath().toString();

            UpdateLoad update = new UpdateLoad(pathStr, graph);

            String graphNodeLabel = getGraphLabel(graph);
            logger.info("Selecting TDB2 workload: " + pathStr + " -> " + graphNodeLabel);
            workloads.add(update);
        }

        Location location = Location.create(outputPath);
        Dataset dataset = TDB2Factory.connectDataset(location);
        try {
            DatasetGraph dg = dataset.asDatasetGraph();

            try (AutoTxn txn = Txn.autoTxn(dg, TxnType.WRITE)) {
                // Run load statements.
                for (UpdateLoad update : workloads) {
                    String source = update.getSource();
                    Node destNode = update.getDest();

                    String destNodeLabel = getGraphLabel(destNode);
                    UpdateExec.dataset(dg).update(update).execute();
                }
                txn.commit();
            }

        } finally {
            dataset.close();
            // StoreConnection.release(location);
            // Not sure whether there is a clean way to remove the lock files
            Files.deleteIfExists(outputPath.resolve("tdb.lock"));
            Files.deleteIfExists(outputPath.resolve("Data-0001/tdb.lock"));
            // ProcessFileLock lock = DatabaseConnection.lockForLocation(TDB2Factory.location(dataset));
//            Path path = lock.getPath();
//            logger.info("TDB2 file lock is: " + path);
//
//            ProcessFileLock.release(lock);
//            Files.delete(path);
        }

        RDFDatabaseTDB2 result = new RDFDatabaseTDB2(outputPath);
        return result;
    }

    @Override
    public RDFDatabase getDatabaseView() {
        return new RDFDatabaseTDB2(outputFolder);
    }
}
