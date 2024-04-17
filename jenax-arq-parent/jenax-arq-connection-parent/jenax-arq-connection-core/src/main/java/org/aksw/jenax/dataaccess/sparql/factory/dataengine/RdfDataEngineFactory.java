package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceFactory;

/** Factory for RdfDataEngine instances based on a configuration object */
public interface RdfDataEngineFactory {
    RdfDataEngine create(Map<String, Object> config) throws Exception;

    public static RdfDataEngineFactory wrap(RdfDataSourceFactory rdfDataSourceFactory) {
        RdfDataEngineFactory tmp = config -> RdfDataEngines.of(rdfDataSourceFactory.create(config));
        return tmp;
    }
}
