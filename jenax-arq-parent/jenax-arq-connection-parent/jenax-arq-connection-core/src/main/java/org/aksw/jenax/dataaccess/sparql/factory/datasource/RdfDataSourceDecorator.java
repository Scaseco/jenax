package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;

public interface RdfDataSourceDecorator
{
    public RDFDataSource decorate(RDFDataSource decoratee, Map<String, Object> options);
}
