package org.aksw.jenax.dataaccess.sparql.dataengine;

import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.apache.jena.rdfconnection.RDFConnection;

/** The main difference between an RdfDataEngine and an RdfDataSource is that
 * an engine in 'active' and implements {@link AutoCloseable} in order to shut down
 * whereas plain rdf data sources are 'passive' and therefore don't need to be closed.
 * Every RDF engine supports opening connections to it and therefore extends RdfDataSource.
 *
 * The same design principle is followed by Spring's EmbeddedDatabase interface.
 */
public interface RdfDataEngine
    extends RdfDataSource, AutoCloseable
{
    @Override
    default RdfDataEngine decorate(RdfDataSourceTransform rdfDataSourceTransform) {
        return RdfDataEngines.transform(this, rdfDataSourceTransform);
    }

    /** Return a new RdfDataEngine whose {@link #getConnection()} method has the given
     *  wrapping applied */
    default RdfDataEngine applyConnectionWrapper(Function<RDFConnection, RDFConnection> wrapper) {
        RdfDataEngine self = this;
        return new RdfDataEngine() {

            @Override
            public RDFConnection getConnection() {
                RDFConnection raw = self.getConnection();
                RDFConnection result = wrapper.apply(raw);
                return result;
            }

            @Override
            public void close() throws Exception {
                self.close();
            }
        };
    }
}
