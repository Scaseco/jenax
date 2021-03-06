package org.aksw.jenax.arq.datasource;

import java.util.Map;

import org.aksw.jenax.connection.datasource.RdfDataSource;

public interface RdfDataSourceDecorator
{
    public RdfDataSource decorate(RdfDataSource decoratee, Map<String, Object> options);
}
