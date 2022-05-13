package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.arq.connection.RDFConnectionModular;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.SparqlQueryConnectionJsa;
import org.aksw.jenax.connection.dataengine.RdfDataEngine;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class RdfDataEngines {
    public static class RdfDataEngineOverRdfDataSource
        implements RdfDataEngine
    {
        protected RdfDataSource rdfDataSource;
        protected AutoCloseable closeAction;

        public RdfDataEngineOverRdfDataSource(RdfDataSource rdfDataSource, AutoCloseable closeAction) {
            super();
            this.rdfDataSource = rdfDataSource;
            this.closeAction = closeAction;
        }

        @Override
        public RDFConnection getConnection() {
            return rdfDataSource.getConnection();
        }

        @Override
        public void close() throws Exception {
        	if (closeAction != null) {
        		closeAction.close();
        	}
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

    public static class RdfDataEngineOverQueryExecutionFactory
		implements RdfDataEngine
	{
		protected QueryExecutionFactory qef;

		public RdfDataEngineOverQueryExecutionFactory(QueryExecutionFactory qef) {
			super();
			this.qef = qef;
		}

		@Override
		public RDFConnection getConnection() {
			SparqlQueryConnection core = new SparqlQueryConnectionJsa(qef);
			RDFConnection result = new RDFConnectionModular(core, null, null);
			return result;
		}

		@Override
		public void close() throws Exception {
			qef.close();
		}
	}

	public static RdfDataEngine adapt(QueryExecutionFactory qef) {
		return new RdfDataEngineOverQueryExecutionFactory(qef);
	}


    /** Wrap an RdfDataSource as an RdfDataEngine */
    public static RdfDataEngine of(RdfDataSource rdfDataSource) {
    	return of(rdfDataSource, null);
    }

    public static RdfDataEngine of(RdfDataSource rdfDataSource, AutoCloseable closeAction) {
        RdfDataEngine result = rdfDataSource instanceof RdfDataEngine
                ? (RdfDataEngine)rdfDataSource
                : new RdfDataEngineOverRdfDataSource(rdfDataSource, closeAction);

        return result;
    }
}
