package org.aksw.jenax.dataaccess.sparql.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RdfDataSourceWrapper<T extends RdfDataSource>
    extends RdfDataSource
{
    T getDelegate();

    @Override
    default RDFConnection getConnection() {
        return getDelegate().getConnection();
    }
}
