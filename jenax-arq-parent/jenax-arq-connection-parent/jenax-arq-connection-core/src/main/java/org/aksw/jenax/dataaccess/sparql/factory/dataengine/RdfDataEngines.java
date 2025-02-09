package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngineDecoratorBase;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngineWrapperBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransforms;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkWrapperWithWorkerThread;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryBase;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;

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

        public RdfDataSource getDataSource() {
            return rdfDataSource;
        }

        public AutoCloseable getCloseAction() {
            return closeAction;
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

    /** A data engine with data source wrappers applied */
    public static class WrappedRdfDataEngine
        extends RdfDataEngineDecoratorBase<RdfDataEngine>
    {
        protected RdfDataSource effectiveDataSource;

        public WrappedRdfDataEngine(RdfDataEngine baseEngine, RdfDataSource effectiveDataSource) {
            super(Objects.requireNonNull(baseEngine));
            this.effectiveDataSource = Objects.requireNonNull(effectiveDataSource);
        }

        @Override
        public RdfDataEngine getDecoratee() {
            return super.getDecoratee();
        }

        public RdfDataSource getEffectiveDataSource() {
            return effectiveDataSource;
        }

        @Override
        public RDFConnection getConnection() {
            return effectiveDataSource.getConnection();
        }
    }

    /** Return the DataEngine as a DatasSource thereby remove any wrapping with RdfDataEngineOverRdfDataSource */
    public static RdfDataSource unwrapDataSource(RdfDataEngineOverRdfDataSource dataEngine) {
        RdfDataSource result = dataEngine;
        while (result instanceof RdfDataEngineOverRdfDataSource) {
            result = ((RdfDataEngineOverRdfDataSource)result).getDataSource();
        }
        return result;
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
            LinkSparqlQuery queryLink = LinkSparqlQueryBase.of(() -> new QueryExecBuilderCustomBase<>() {
                @Override
                public QueryExec build() {
                    // TODO Raise warnings when unsupported features are requested.
                    String str = this.getQueryString();
                    QueryExecution qe = str != null
                            ? qef.createQueryExecution(str)
                            : qef.createQueryExecution(getParsedQuery());
                    return QueryExecAdapter.adapt(qe);
                }
            });
            return RDFConnectionAdapter.adapt(new RDFLinkModular(queryLink, null, null));
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
                RDFConnection result = RDFConnectionUtils.wrapWithLinkTransform(base, xform);
                return result;
            }
        };
    }

    public static RdfDataEngine adapt(QueryExecutionFactory qef) {
        return new RdfDataEngineOverQueryExecutionFactory(qef);
    }


    /**
     * Returns the argument if it already is a RdfDataEngine.
     * Otherwise, wrap an RdfDataSource as an RdfDataEngine with a no-op close method.
     */
    public static RdfDataEngine of(RdfDataSource rdfDataSource) {
        return rdfDataSource instanceof RdfDataEngine
                ? (RdfDataEngine)rdfDataSource
                : of(rdfDataSource, null);
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

    /**
     * If the dataSource is already a data engine then return it.
     *
     * Otherwise:
     * (a) If the baseDataEngine is non-null, then return a WrappedRdfDataEngine from both arguments.
     *
     * (b) If the baseDataEngine is null, then just wrap the RdfDataSource as a RdfDataEngine with a
     * no-op close action.
     *
     * @param dataSource
     * @param baseDataEngine
     * @return
     */
    public static RdfDataEngine of(RdfDataSource dataSource, RdfDataEngine baseDataEngine) {
        RdfDataEngine result = dataSource instanceof RdfDataEngine
            ? (RdfDataEngine)dataSource
            : baseDataEngine == null
                ? new RdfDataEngineOverRdfDataSource(dataSource, null)
                : new WrappedRdfDataEngine(asWrappedEngine(baseDataEngine).getDecoratee(), dataSource);
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

        return new RdfDataEngineDecoratorBase<>(dataEngine) {
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

//    public static RdfDataEngine wrapWithOpTransform(RdfDataEngine dataEngine, Transform statelessTransform) {
//        return wrapWithOpTransform(dataEngine, () -> statelessTransform);
//    }
//
//    /**
//     * Wrap a datasource such that an algebra transform is applied to each statement.
//     */
//    // XXX Improve method to flatten multiple stacking transforms on a dataSource into a
//    //   list which makes debugging easier
//    public static RdfDataSource wrapWithOpTransform(RdfDataEngine dataEngine, Supplier<Transform> transformSupplier) {
//        return of(unwrapDataSource(dataEngine));
//    }
//
    public static RdfDataEngine wrapWithStmtTransform(RdfDataEngine dataEngine, SparqlStmtTransform stmtTransform) {
        RdfDataSourceTransform dataSourceTransform = RdfDataSourceTransforms.of(stmtTransform);
        return wrapWithDataSourceTransform(dataEngine, dataSourceTransform);
    }

    public static RdfDataEngine wrapWithDataSourceTransform(RdfDataEngine dataEngine, RdfDataSourceTransform transform) {
        WrappedRdfDataEngine tmp = asWrappedEngine(dataEngine);
        RdfDataSource before = tmp.getEffectiveDataSource();
        RdfDataSource after = transform.apply(before);
        return new WrappedRdfDataEngine(dataEngine, after);
    }

    public static WrappedRdfDataEngine asWrappedEngine(RdfDataEngine dataEngine) {
        return dataEngine instanceof WrappedRdfDataEngine
            ? (WrappedRdfDataEngine)dataEngine
            : new WrappedRdfDataEngine(dataEngine, dataEngine);
    }
}
