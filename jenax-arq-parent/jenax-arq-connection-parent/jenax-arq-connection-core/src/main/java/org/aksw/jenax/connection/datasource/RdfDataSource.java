package org.aksw.jenax.connection.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

/**
 * A factory/supplier of RDFConnection instances. Similar to a JDBC DataSource.
 *
 * In cases where multiple threads need to operate on a database it is much cleaner
 * having each thread operate on its own connection rather than sharing a single instance.
 *
 * Typically connection pooling is built on top of a data source interface in order to speed up
 * handing out connections to clients.
 *
 * For example, with a single connection there are many cases where it is not clear what will happen:
 * - One thread is retrieving a result set while another thread concurrently starts another query/update
 * - Will concurrent access break transactions?
 * - etc.
 *
 */
public interface RdfDataSource {
    RDFConnection getConnection();
}
