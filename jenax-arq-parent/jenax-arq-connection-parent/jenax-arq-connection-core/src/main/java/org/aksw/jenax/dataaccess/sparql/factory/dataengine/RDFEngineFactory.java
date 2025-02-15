package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceFactory;

/** Factory for RdfDataEngine instances based on a configuration object */
public interface RDFEngineFactory {
    default RdfDataEngine create(Map<String, Object> config) throws Exception {
        return newEngineBuilder().setProperties(config).build();
    }

    /** Return a builder for a data engine factory. Must never return null. */
    RDFEngineBuilder<?> newEngineBuilder();

    public static RDFEngineFactory wrap(RdfDataSourceFactory rdfDataSourceFactory) {
        return new RdfDataEngineFactoryOverRdfDataSourceFactory(rdfDataSourceFactory);
    }
}
