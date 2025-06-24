package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;

public interface RdfDataSourceFactory {
    RDFDataSource create(Map<String, Object> config) throws Exception;
}
