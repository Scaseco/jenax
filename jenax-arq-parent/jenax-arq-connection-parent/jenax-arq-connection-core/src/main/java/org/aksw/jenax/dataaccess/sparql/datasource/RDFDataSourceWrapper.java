package org.aksw.jenax.dataaccess.sparql.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFDataSourceWrapper<T extends RDFDataSource>
    extends RDFDataSource
{
    T getDelegate();

    @Override
    default RDFConnection getConnection() {
        return getDelegate().getConnection();
    }
}
