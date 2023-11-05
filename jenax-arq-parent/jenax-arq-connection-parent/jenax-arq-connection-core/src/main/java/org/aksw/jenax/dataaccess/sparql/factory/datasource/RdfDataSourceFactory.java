package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;

public interface RdfDataSourceFactory {
    RdfDataSource create(Map<String, Object> config) throws Exception;
}
