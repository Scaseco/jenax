package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.connection.dataengine.RdfDataEngine;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.rdfconnection.RDFConnection;

public class RdfDataEngines {
    public static class RdfDataEngineFromRdfDataSource
        implements RdfDataEngine
    {
        protected RdfDataSource rdfDataSource;

        public RdfDataEngineFromRdfDataSource(RdfDataSource rdfDataSource) {
            super();
            this.rdfDataSource = rdfDataSource;
        }

        @Override
        public RDFConnection getConnection() {
            return rdfDataSource.getConnection();
        }

        @Override
        public void close() throws Exception {
            // no op
        }
    }

    /** Decorate an RdfDataEngine with an rdfDataSource decorator */
    public static RdfDataEngine decorate(RdfDataEngine rdfDataEngine, RdfDataSourceDecorator decorator) {
        RdfDataSource wrapped = decorator.decorate(rdfDataEngine, null);

        return new RdfDataEngine() {
            @Override
            public void close() throws Exception {
                rdfDataEngine.close();
            }
            @Override
            public RDFConnection getConnection() {
                RDFConnection r = wrapped.getConnection();
                return r;
            }
        };
    }

    /** Wrap an RdfDataSource as an RdfDataEngine */
    public static RdfDataEngine wrap(RdfDataSource rdfDataSource) {
        RdfDataEngine result = rdfDataSource instanceof RdfDataEngine
                ? (RdfDataEngine)rdfDataSource
                : new RdfDataEngineFromRdfDataSource(rdfDataSource);

        return result;
    }
}
