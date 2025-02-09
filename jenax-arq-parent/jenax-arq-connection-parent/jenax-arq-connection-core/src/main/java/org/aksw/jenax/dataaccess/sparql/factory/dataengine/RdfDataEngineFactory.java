package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseBuilder;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceFactory;

/** Factory for RdfDataEngine instances based on a configuration object */
public interface RdfDataEngineFactory {
    default RdfDataEngine create(Map<String, Object> config) throws Exception {
        return newEngineBuilder().setProperties(config).build();
    }

    /** Return a builder for a data engine factory. Must never return null. */
    RdfDataEngineBuilder<?> newEngineBuilder();

    /**
     * Return a builder to create databases or null if this feature is not supported.
     * Whether or not the database builder starts a database engine
     * is up to the implementation.
     */
    default RdfDatabaseBuilder<?> newDatabaseBuilder() {
        return null;
    }

    public static RdfDataEngineFactory wrap(RdfDataSourceFactory rdfDataSourceFactory) {
        return new RdfDataEngineFactoryOverRdfDataSourceFactory(rdfDataSourceFactory);
    }
}
