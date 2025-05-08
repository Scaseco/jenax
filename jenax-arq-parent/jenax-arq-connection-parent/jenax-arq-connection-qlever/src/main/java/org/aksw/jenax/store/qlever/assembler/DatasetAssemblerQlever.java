package org.aksw.jenax.store.qlever.assembler;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.jenax.dataaccess.sparql.creator.FileSet;
import org.aksw.jenax.dataaccess.sparql.dataset.engine.DatasetGraphOverRDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.engine.qlever.RdfDatabaseBuilderQlever;
import org.aksw.jenax.engine.qlever.RdfDatabaseQlever;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;

import jenax.engine.qlever.docker.QleverConfRun;
import jenax.engine.qlever.docker.RDFEngineBuilderQlever;

/** */
public class DatasetAssemblerQlever
    extends DatasetAssembler
{
    @Override
    protected DatasetGraph createDataset(Assembler a, Resource root) {
        QleverConfig res = new QleverConfig(root.asNode(), (EnhGraph)root.getModel());

        QleverConfRun confRun = new QleverConfRun();
        res.copyInto(confRun, false);

        // Location is currently a separate property because it needs to be fleshed out
        // how to bridge locations as strings and java nio paths (virtual file system support).
        String location = res.getLocation();
        if (location == null) {
            throw new AssemblerException(root, "Required location is missing: " + QleverAssemblerVocab.location);
        }

        String indexName = confRun.getIndexBaseName();
        if (indexName == null) {
            throw new AssemblerException(root, "Required index name is missing: " + QleverAssemblerVocab.indexName);
        }

        Path path = Path.of(location);
        FileSet fileSet = RdfDatabaseQlever.getFileSet(path, indexName);

        // If the database does not exist then create the database.
        RdfDatabaseQlever db;
        try {
            if (!fileSet.isEmpty()) {
                db = new RdfDatabaseQlever(path, indexName);
            } else {
                // Create the database if it does not exist yet
                RdfDatabaseBuilderQlever dbBuilder = new RdfDatabaseBuilderQlever();
                dbBuilder.setIndexName(indexName);
                dbBuilder.setOutputFolder(path);
                dbBuilder.addPath("/home/raven/Datasets/text2sparql2025/corporate-kg/corporate-kg-1.0.0-prod-inst.ttl");
                db = dbBuilder.build();
                fileSet = db.getFileSet();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        RDFEngineBuilderQlever<?> engineBuilder = new RDFEngineBuilderQlever<>();

        RDFEngine engine;
        try {
            engine = engineBuilder
                // .setLocation(location)
                // .setDatabase(database)
                // .setLocation(systemName)
                // .setIndexName(systemName)
                .setConfig(confRun)
                .setDatabase(db)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DatasetGraph result = DatasetGraphOverRDFEngine.of(engine);
        return result;
    }
}


//RDFEngine engine;
//try (Closer closer = Closer.create()) {
//  try {
//      RDFEngineBuilderQlever<?> engineBuilder = new RDFEngineBuilderQlever<>();
//      // RDFEngineFactory engineFactory = registry.getEngineFactory(systemName);
//      engine = engineBuilder
//          // .setDatabase(database)
//          // .setLocation(systemName)
//          // .setIndexName(systemName)
//          .setConfig(confRun)
//          // .setProperty("accessToken", "abcde")
//          .build();
//  } catch (Throwable t) {
//      throw closer.rethrow(t);
//  } finally {
//      closer.close();
//  }
//} catch (IOException e) {
//  throw new RuntimeException(e);
//}

