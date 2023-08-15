package org.aksw.jenax.arq.datasource;

import java.util.function.Function;

import org.aksw.jenax.arq.connection.RDFConnectionModular;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.connection.core.SparqlQueryConnectionJsa;
import org.aksw.jenax.connection.dataengine.RdfDataEngine;
import org.aksw.jenax.connection.dataengine.RdfDataEngineDecoratorBase;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.exec.QueryExec;

public class RdfDataEngines {
    /**
     * This method creates an RdfDataSource view over a connection.
     * Wrapping a connection as an engine is more a hack and should be avoided.
     */
    @Deprecated
    public static RdfDataEngine ofQueryConnection(SparqlQueryConnection conn) {
        return new RdfDataEngineOverSparqlQueryConnection(new RDFConnectionModular(conn, null, null));
    }

    public static class RdfDataEngineOverSparqlQueryConnection
        implements RdfDataEngine
    {
        protected RDFConnection conn;

        public RdfDataEngineOverSparqlQueryConnection(RDFConnection conn) {
            super();
            this.conn = conn;
        }

        @Override
        public RDFConnection getConnection() {
            return RDFConnectionUtils.withCloseShield(conn);
        }

        @Override
        public void close() throws Exception {
            conn.close();
        }
    }

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

    public static RdfDataEngine of(Dataset dataset) {
        return RdfDataEngineFromDataset.create(dataset, true);
    }

    /**
     * Return a new RdfDataEngine with the given query and queryExec transforms applied.
     *
     * @param dataEngine
     * @param queryTransform
     * @param queryExecTransform
     * @return
     */
    public static RdfDataEngine wrapWithQueryTransform(
            RdfDataEngine dataEngine,
            Function<? super Query, ? extends Query> queryTransform,
            Function<? super QueryExec, ? extends QueryExec> queryExecTransform
            ) {

        return new RdfDataEngineDecoratorBase<RdfDataEngine>(dataEngine) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection raw = decoratee.getConnection();
                RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(raw, queryTransform, queryExecTransform);
                return result;
            }
        };
    }
}
