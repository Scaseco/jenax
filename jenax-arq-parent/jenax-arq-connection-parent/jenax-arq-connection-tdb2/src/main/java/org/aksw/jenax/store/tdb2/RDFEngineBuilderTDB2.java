package org.aksw.jenax.store.tdb2;

import java.nio.file.Path;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineBuilderBase;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;

public class RDFEngineBuilderTDB2<X extends RdfDataEngineBuilderBase<X>>
    extends RdfDataEngineBuilderBase<X>
{
    @Override
    public RDFEngine build() throws Exception {
        RDFDatabase rawDb = Objects.requireNonNull(getDatabase());
        RDFDatabaseTDB2 db = (RDFDatabaseTDB2)rawDb;
        Path path = db.getPath();
        Location location = Location.create(path);
        Dataset dataset = TDB2Factory.connectDataset(location);
        RDFEngine result = RDFEngines.of(dataset.asDatasetGraph(), true);
        return result;
    }
}
