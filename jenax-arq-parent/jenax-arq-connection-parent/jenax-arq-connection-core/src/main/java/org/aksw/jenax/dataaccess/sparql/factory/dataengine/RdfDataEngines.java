package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsa;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngineDecoratorBase;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngineWrapperBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkWrapperWithWorkerThread;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

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

    public static RdfDataEngine transform(RdfDataEngine dataEngine, RdfDataSourceTransform transform) {
        RdfDataSource dataSource = transform.apply(dataEngine);
        return of(dataSource, dataEngine::close);
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

    /** Return a new RdfDataEngine which applies the given transformation to each created link */
    public static RdfDataEngine wrapWithLinkTransform(RdfDataEngine dataEngine, RDFLinkTransform xform) {
        return new RdfDataEngineWrapperBase<RdfDataEngine>(dataEngine) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection base = getDelegate().getConnection();
                RDFConnection result = RDFConnectionUtils.wrapWithLinkDecorator(base, xform);
                return result;
            }
        };
    }

    public static RdfDataEngine adapt(QueryExecutionFactory qef) {
        return new RdfDataEngineOverQueryExecutionFactory(qef);
    }


    /** Wrap an RdfDataSource as an RdfDataEngine */
    public static RdfDataEngine of(RdfDataSource rdfDataSource) {
        return of(rdfDataSource, null);
    }

    /**
     * Wrap an {@link RdfDataSource} with a close action to create an RdfDataEngine.
     *
     * If the provided data source is already an RdfDataEngine and the close action should just delegate to it
     * do not use the lambda <pre>() -> dataEngine.close()</pre> because it introduces needless wrapping.
     * In this case use null.
     */
    public static RdfDataEngine of(RdfDataSource rdfDataSource, AutoCloseable closeAction) {
        RdfDataEngine result = rdfDataSource instanceof RdfDataEngine && (closeAction == null || closeAction == rdfDataSource)
                ? (RdfDataEngine)rdfDataSource
                : new RdfDataEngineOverRdfDataSource(rdfDataSource, closeAction);

        return result;
    }

    public static RdfDataEngine of(Model model) {
        return of(DatasetFactory.wrap(model));
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
            QueryTransform queryTransform,
            QueryExecTransform queryExecTransform
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

    public static RdfDataEngine wrapWithAutoTxn(RdfDataEngine dataEngine, Dataset dataset) {
        return of(RdfDataSources.wrapWithAutoTxn(dataEngine, dataset), dataEngine);
    }

    public static RdfDataEngine wrapWithWorkerThread(RdfDataEngine dataEngine) {
        return wrapWithLinkTransform(dataEngine, RDFLinkWrapperWithWorkerThread::wrap);
    }
}
