package org.aksw.jenax.dataaccess.sparql.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RdfDataSourceWrapper
    extends RdfDataSource
{
    RdfDataSource getDelegate();

    @Override
    default RDFConnection getConnection() {
        return getDelegate().getConnection();
    }
}
