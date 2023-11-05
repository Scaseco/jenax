package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;

public interface RdfDataSourceDecorator
{
    public RdfDataSource decorate(RdfDataSource decoratee, Map<String, Object> options);
}
