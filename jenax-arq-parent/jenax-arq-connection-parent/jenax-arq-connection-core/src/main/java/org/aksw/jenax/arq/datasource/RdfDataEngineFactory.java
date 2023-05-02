package org.aksw.jenax.arq.datasource;

import java.util.Map;

import org.aksw.jenax.connection.dataengine.RdfDataEngine;

public interface RdfDataEngineFactory {
    RdfDataEngine create(Map<String, Object> config) throws Exception;

    public static RdfDataEngineFactory wrap(RdfDataSourceFactory rdfDataSourceFactory) {
        RdfDataEngineFactory tmp = config -> RdfDataEngines.of(rdfDataSourceFactory.create(config));
        return tmp;
    }
}