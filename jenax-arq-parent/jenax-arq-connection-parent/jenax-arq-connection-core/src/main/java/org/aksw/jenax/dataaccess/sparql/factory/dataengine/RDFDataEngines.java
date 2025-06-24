package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryBase;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.exec.QueryExec;

public class RDFDataEngines {
    /**
     * This method creates an RdfDataSource view over a connection.
     * Wrapping a connection as an engine is more a hack and should be avoided.
     */
//    @Deprecated
//    public static RDFEngine ofQueryConnection(SparqlQueryConnection conn) {
//        return new RdfDataEngineOverSparqlQueryConnection(new RDFConnectionModular(conn, null, null));
//    }

//    public static class RdfDataEngineOverSparqlQueryConnection
//        implements RDFEngine
//    {
//        protected RDFConnection conn;
//
//        public RdfDataEngineOverSparqlQueryConnection(RDFConnection conn) {
//            super();
//            this.conn = conn;
//        }
//
////        @Override
////        public RDFConnection getConnection() {
////            return RDFConnectionUtils.withCloseShield(conn);
////        }
//
//        @Override
//        public void close() throws Exception {
//            conn.close();
//        }
//
//        @Override
//        public RDFLinkBuilder newLinkBuilder() {
//            return new RDFLinkBuilderOverLinkSupplier(() -> RDFLinkAdapter.adapt(conn));
//        }
//
//        @Override
//        public DatasetGraph getDataset() {
//            return null;
//        }
//    }

//    public static class RdfDataEngineOverRdfDataSource
//        implements RDFEngine
//    {
//        protected RDFDataSource rdfDataSource;
//        protected AutoCloseable closeAction;
//
//        public RdfDataEngineOverRdfDataSource(RDFDataSource rdfDataSource, AutoCloseable closeAction) {
//            super();
//            this.rdfDataSource = rdfDataSource;
//            this.closeAction = closeAction;
//        }
//
//        public RDFDataSource getDataSource() {
//            return rdfDataSource;
//        }
//
//        public AutoCloseable getCloseAction() {
//            return closeAction;
//        }
//
//        @Override
//        public RDFConnection getConnection() {
//            return rdfDataSource.getConnection();
//        }
//
//        @Override
//        public void close() throws Exception {
//            if (closeAction != null) {
//                closeAction.close();
//            }
//        }
//    }

    /** Return the DataEngine as a DatasSource thereby remove any wrapping with RdfDataEngineOverRdfDataSource */
//    public static RDFDataSource unwrapDataSource(RdfDataEngineOverRdfDataSource dataEngine) {
//        RDFDataSource result = dataEngine;
//        while (result instanceof RdfDataEngineOverRdfDataSource) {
//            result = ((RdfDataEngineOverRdfDataSource)result).getDataSource();
//        }
//        return result;
//    }

//    public static RDFEngine transform(RDFEngine dataEngine, RdfDataSourceTransform transform) {
//        RDFDataSource dataSource = transform.apply(dataEngine);
//        return of(dataSource, dataEngine::close);
//    }

    /** Decorate an RdfDataEngine with an rdfDataSource decorator */
//    public static RDFEngine decorate(RDFEngine rdfDataEngine, RdfDataSourceDecorator decorator) {
//        RDFDataSource wrapped = decorator.decorate(rdfDataEngine, null);
//
//        return new RDFEngine() {
//            @Override
//            public void close() throws Exception {
//                rdfDataEngine.close();
//            }
//            @Override
//            public RDFConnection getConnection() {
//                RDFConnection r = wrapped.getConnection();
//                return r;
//            }
//        };
//    }

    public static class RDFLinkSourceOverQueryExecutionFactory
        implements RDFLinkSource
    {
        protected QueryExecutionFactory qef;

        public RDFLinkSourceOverQueryExecutionFactory(QueryExecutionFactory qef) {
            super();
            this.qef = qef;
        }

        @Override
        public RDFLinkBuilder<?> newLinkBuilder() {
            return new RDFLinkBuilderOverLinkSupplier(() -> {

        // public RDFConnection getConnection() {
            LinkSparqlQuery queryLink = LinkSparqlQueryBase.of(() -> new QueryExecBuilderCustomBase<>() {
                @Override
                public QueryExec build() {
                    // TODO Raise warnings when unsupported features are requested.
                    String str = this.getQueryString();
                    QueryExecution qe = str != null
                            ? qef.createQueryExecution(str)
                            : qef.createQueryExecution(getParsedQuery());
                    return QueryExec.adapt(qe);
                    }
                });
                return new RDFLinkModular(queryLink, null, null);
            });
            // return RDFConnectionAdapter.adapt();
        }
//        @Override
//        public void close() throws Exception {
//            qef.close();
//        }
//
//        @Override
//        public DatasetGraph getDataset() {
//            return null;
//        }
    }

    /** Return a new RdfDataEngine which applies the given transformation to each created link */
//    public static RDFEngine wrapWithLinkTransform(RDFEngine dataEngine, RDFLinkTransform xform) {
//        return new RdfDataEngineWrapperBase<RDFEngine>(dataEngine) {
//            @Override
//            public RDFConnection getConnection() {
//                RDFConnection base = getDelegate().getConnection();
//                RDFConnection result = RDFConnectionUtils.wrapWithLinkTransform(base, xform);
//                return result;
//            }
//        };
//    }

    public static RDFEngine adapt(QueryExecutionFactory qef) {
        RDFLinkSource linkSource = new RDFLinkSourceOverQueryExecutionFactory(qef);
        return RDFEngines.of(linkSource, qef::close);
    }

    /**
     * Returns the argument if it already is a RdfDataEngine.
     * Otherwise, wrap an RdfDataSource as an RdfDataEngine with a no-op close method.
     */
//    public static RDFEngine of(RDFDataSource rdfDataSource) {
//        return rdfDataSource instanceof RDFEngine
//                ? (RDFEngine)rdfDataSource
//                : of(rdfDataSource, null);
//    }

    /**
     * Wrap an {@link RDFDataSource} with a close action to create an RdfDataEngine.
     *
     * If the provided data source is already an RdfDataEngine and the close action should just delegate to it
     * do not use the lambda <pre>() -> dataEngine.close()</pre> because it introduces needless wrapping.
     * In this case use null.
     */
//    public static RDFEngine of(RDFDataSource rdfDataSource, AutoCloseable closeAction) {
//        RDFEngine result;
//        if (rdfDataSource instanceof RDFEngine rde) {
//            if (closeAction == null || closeAction == rdfDataSource) {
//                result = (RDFEngine)rdfDataSource;
//            } else {
//                result = new RdfDataEngineOverRdfDataSource(rde, () -> {
//                    try {
//                        rde.close();
//                    } finally {
//                        closeAction.close();
//                    }
//                });
//            }
//        } else {
//            result = new RdfDataEngineOverRdfDataSource(rdfDataSource, closeAction);
//        }
//        return result;
//    }

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
//    public static RDFEngine of(RDFDataSource dataSource, RDFEngine baseDataEngine) {
//        RDFEngine result = dataSource instanceof RDFEngine
//            ? (RDFEngine)dataSource
//            : baseDataEngine == null
//                ? new RdfDataEngineOverRdfDataSource(dataSource, null)
//                : new RDFEngineDecorator(asWrappedEngine(baseDataEngine).getDelegate(), dataSource);
//        return result;
//    }

    public static RDFEngine of(Model model) {
        return of(DatasetFactory.wrap(model));
    }

    public static RDFEngine of(Dataset dataset) {
        return new RdfDataEngineFromDataset(dataset.asDatasetGraph(), true);
    }

    /**
     * Return a new RdfDataEngine with the given query and queryExec transforms applied.
     *
     * @param dataEngine
     * @param queryTransform
     * @param queryExecTransform
     * @return
     */
//    public static RDFEngine wrapWithQueryTransform(
//            RDFEngine dataEngine,
//            QueryTransform queryTransform,
//            QueryExecTransform queryExecTransform
//            ) {
//
//        return new RDFEngineWrapperBase<>(dataEngine) {
//            @Override
//            public RDFConnection getConnection() {
//                RDFConnection raw = delegate.getConnection();
//                RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(raw, queryTransform, queryExecTransform);
//                return result;
//            }
//        };
//    }

//    public static RDFEngine wrapWithAutoTxn(RDFEngine dataEngine, Dataset dataset) {
//        // RDFLinkSources.wrapWithAutoTxn(null, dataset)
//        return of(RdfDataSources.wrapWithAutoTxn(dataEngine, dataset), dataEngine);
//    }
//
//    public static RDFEngine wrapWithWorkerThread(RDFEngine dataEngine) {
//        return wrapWithLinkTransform(dataEngine, RDFLinkWrapperWithWorkerThread::wrap);
//    }
//
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
//    public static RDFEngine wrapWithExprTransform(RDFEngine dataEngine, ExprTransform exprTransform) {
//        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.ofExprTransform(exprTransform);
//        return wrapWithStmtTransform(dataEngine, stmtTransform);
//    }
//
//    public static RDFEngine wrapWithStmtTransform(RDFEngine dataEngine, SparqlStmtTransform stmtTransform) {
//        RdfDataSourceTransform dataSourceTransform = RdfDataSourceTransforms.of(stmtTransform);
//        return wrapWithDataSourceTransform(dataEngine, dataSourceTransform);
//    }
//
//    public static RDFEngine wrapWithDataSourceTransform(RDFEngine dataEngine, RdfDataSourceTransform transform) {
//        RDFEngineDecorator tmp = asWrappedEngine(dataEngine);
//        RDFDataSource before = tmp.getDataSource();
//        RDFDataSource after = transform.apply(before);
//        return new RDFEngineDecorator(dataEngine, after);
//    }
//
//    public static RDFEngineDecorator<RDFEngine> asWrappedEngine(RDFEngine dataEngine) {
//        return dataEngine instanceof RDFEngineDecorator de
//            ? (RDFEngineDecorator)de
//            : RDFEngineDecorator.of(dataEngine);
//    }
//
//    public static RDFEngine wrapWithCloseAction(RDFEngine delegate, Closeable closeable) {
//        return RDFEngineDecorator.of(delegate).addCloseAction(closeable);
//        // return RdfDataEngines.of(delegate, closeable);
//    }
//
//
//    public static RDFEngine decorate(RDFEngine base, RdfDataSourceTransform transform) {
//        RDFEngine result = wrapWithDataSourceTransform(base, transform);
//        return result;
//    }
//
//    public static RDFEngine decorate(RDFEngine base, RDFLinkTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, QueryTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, QueryExecTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, UpdateRequestTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, RDFLinkSourceTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, LinkSparqlQueryTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, SparqlStmtTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    // OpTransform
//    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
//    public static RDFEngine decorate(RDFEngine base, Rewrite transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
//
//    public static RDFEngine decorate(RDFEngine base, ExprTransform transform) {
//        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
//        return decorate(base, tmp);
//    }
}
