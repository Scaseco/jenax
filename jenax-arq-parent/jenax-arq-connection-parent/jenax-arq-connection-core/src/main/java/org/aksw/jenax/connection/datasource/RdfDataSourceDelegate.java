package org.aksw.jenax.connection.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RdfDataSourceDelegate
    extends RdfDataSource
{
    RdfDataSource getDelegate();

    @Override
    default RDFConnection getConnection() {
        return getDelegate().getConnection();
    }
}
