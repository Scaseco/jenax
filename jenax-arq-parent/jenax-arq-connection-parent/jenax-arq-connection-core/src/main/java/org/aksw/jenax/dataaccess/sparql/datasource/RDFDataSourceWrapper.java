package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceWrapper;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * A wrapper for another data source.
 * <p>
 *
 * <b>Use of this wrapper should be avoided in favor of
 * {@link RDFLinkSourceWrapper}</b>.
 * This wrapper exists in order to cope with custom implementations
 * that were inadequately written against {@link RDFDataSource}
 * instead of {@link RDFLinkSource}.
 *
 * <p>
 * To consistently change the behavior of all methods, such as
 * {@linkplain #newQuery()} and {@linkplain #newUpdate()}
 * only the {@link #getConnection()} method needs to be overridden.
 */
public interface RDFDataSourceWrapper<T extends RDFDataSource>
    extends RDFDataSource
{
    T getDelegate();

    /** By default, use the delegate's connection. */
    @Override
    default RDFConnection getConnection() {
        return getDelegate().getConnection();
    }
}
