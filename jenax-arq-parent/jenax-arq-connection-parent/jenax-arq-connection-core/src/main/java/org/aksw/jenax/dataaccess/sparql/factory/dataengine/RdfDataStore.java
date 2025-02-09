package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseBuilder;
import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFactory;

/**
 * A helper data structure to bundle a specific combination of a
 * {@link RdfDataEngineFactory} and a {@link RdfDatabaseBuilder}.
 */
public class RdfDataStore {
    protected RdfDataEngineFactory engineFactory;
    protected RdfDatabaseFactory databaseFactory;

    public RdfDataStore(RdfDataEngineFactory engineFactory, RdfDatabaseFactory databaseFactory) {
        super();
        this.engineFactory = Objects.requireNonNull(engineFactory);
        this.databaseFactory = databaseFactory;
    }

    public RdfDataEngineFactory getEngineFactory() {
        return engineFactory;
    }

    public RdfDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }
}
