package org.aksw.jenax.connection.dataengine;

import org.aksw.jenax.connection.datasource.RdfDataSource;

/** The main difference between an RdfDataEngine and an RdfDataSource is that
 * an engine in 'active' and implements {@link AutoCloseable} in order to shut down
 * whereas plain rdf data sources are 'passive' and therefore don't need to be closed.
 * Every RDF engine supports opening connections to it and therefore extends RdfDataSource.
 *
 * The same design principle is followed by Spring's EmbeddedDatabase system.
 */
public interface RdfDataEngine
    extends RdfDataSource, AutoCloseable
{
}
