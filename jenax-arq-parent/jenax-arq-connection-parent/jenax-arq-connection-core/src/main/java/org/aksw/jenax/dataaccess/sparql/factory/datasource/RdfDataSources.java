package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceOverDataset;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransforms;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSources;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfDataSources {

    private static final Logger logger = LoggerFactory.getLogger(RdfDataSources.class);

    /**
     * Create a datasource where any attempt to <b>execute</b> a query or an update will fail.
     * @implNote The implementation internally decorates an RdfDataSource over a read-only empty dataset.
     */
    public static RDFDataSource alwaysFail() {
        // Using the remote builder defers parsing query strings until the execution
        // The builder for local dataset connections parses query eagerly.
        RDFDataSource dummy = () -> RDFConnectionRemote.newBuilder().destination("urn:dummy").parseCheckSPARQL(false).build();
        // RdfDataSource dummy = of(DatasetFactory.empty());
        RDFDataSource result = RdfDataSources.decorate(dummy, org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceTransforms.alwaysFail());
        return result;
    }

    public static RDFDataSource of(Graph graph) {
        Model model = ModelFactory.createModelForGraph(graph);
        return of(model);
    }

    public static RDFDataSource of(Model model) {
        Dataset ds = DatasetFactory.wrap(model);
        return of(ds);
    }

    public static RDFDataSource of(DatasetGraph dsg) {
        Dataset ds = DatasetFactory.wrap(dsg);
        return of(ds);
    }

    public static RDFDataSource of(Dataset dataset) {
        return new RDFDataSourceOverDataset(dataset);
    }

    /**
     * Execute a query and invoke a function on the response.
     * Upon returning the internally freshly obtained connection and query execution are closed so
     * the result must be detached from those resources.
     */
    public static <T> T exec(RDFDataSource dataSource, Query query, Function<? super QueryExecution, T> qeToResult) {
        Object[] tmp = new Object[] { null };
        try (RDFConnection conn = dataSource.getConnection()) {
            Txn.executeRead(conn, () -> {
                try (QueryExecution qe = conn.query(query)) {
                    tmp[0] = qeToResult.apply(qe);
                }
            });
        }
        @SuppressWarnings("unchecked")
        T result = (T)tmp[0];
        return result;
    }

    /** Reads the 'engine' attribute from the options (if absent defaults to 'mem')
     *  and instantiates the appropriate data source - if possible */
    public static RDFEngine setupRdfDataSource(Map<String, Object> options) throws Exception {
        RdfDataSourceSpecBasicFromMap spec = RdfDataSourceSpecBasicFromMap.wrap(options);

        String sourceType = Optional.ofNullable(spec.getEngine()).orElse("mem");

        RDFEngineFactory factory = RDFEngineFactoryRegistry.get().getFactory(sourceType);
        if (factory == null) {
            throw new RuntimeException("No RdfDataSourceFactory registered under name " + sourceType);
        }

        RDFEngine result = factory.create(options);
        return result;
    }

    /** Utility method that bridges an RdfDataSource to a function that operates on a RDFConnection. */
    public static <T> T compute(RDFDataSource dataSource, Function<RDFConnection, T> computation) {
        T result;
        try (RDFConnection conn = dataSource.getConnection()) {
            result = computation.apply(conn);
        }
        return result;
    }

    public static String fetchDatasetHash(RDFDataSource dataSource) {
        String result = QueryExecutionFactories.fetchDatasetHash(dataSource.asQef());
        return result;
    }


    // --- Legacy

    /**
     * This method creates an RdfDataSource view over a connection.
     * Wrapping a connection as an engine is more a hack and should be avoided.
     */
    @Deprecated
    public static RDFDataSource ofQueryConnection(SparqlQueryConnection conn) {
        return new RDFDataSourceOverSparqlQueryConnection(new RDFConnectionModular(conn, null, null));
    }

    public static class RDFDataSourceOverSparqlQueryConnection
        implements RDFDataSource
    {
        protected RDFConnection conn;

        public RDFDataSourceOverSparqlQueryConnection(RDFConnection conn) {
            super();
            this.conn = conn;
        }

        @Override
        public RDFConnection getConnection() {
            return RDFConnectionUtils.withCloseShield(conn);
        }

//      @Override
//      public void close() throws Exception {
//          conn.close();
//      }
    }

//    /**
//     * Wrap a datasource such that a (stateless) algebra transform is applied to each statement.
//     */
//    public static RDFDataSource wrapWithOpTransform(RDFDataSource dataSource, Transform statelessTransform) {
//        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(statelessTransform);
//        return wrapWithStmtTransform(dataSource, stmtTransform);
//    }
//
//    /**
//     * Wrap a datasource such that an algebra transform is applied to each statement.
//     */
//    // XXX Improve method to flatten multiple stacking transforms on a dataSource into a
//    //   list which makes debugging easier
//    public static RDFDataSource wrapWithOpTransform(RDFDataSource dataSource, Supplier<Transform> transformSupplier) {
//        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(transformSupplier);
//        return wrapWithStmtTransform(dataSource, stmtTransform);
//    }
//
//    /**
//     * Returns a new data source that applies the given rewrite.
//     *
//     * In order to reduce Algebra.compile / OpAsQuery.asQuery round trips,
//     * if the base data source is already a {@link RdfDataSourceWrapperWithRewrite} then
//     * the rewrite is unwrapped and combined with the given rewrite.
//     */
//    public static RDFDataSource wrapWithOpTransform(RDFDataSource dataSource, Rewrite rewrite) {
//        RDFDataSource result;
//        if (dataSource instanceof RdfDataSourceWrapperWithRewrite) {
//            RdfDataSourceWrapperWithRewrite<?> tmp = (RdfDataSourceWrapperWithRewrite<?>)dataSource;
//
//            Rewrite before = tmp.getRewrite();
//            RDFDataSource delegate = tmp.getDelegate();
//            Rewrite effectiveRewrite = RewriteList.flatten(true, before, rewrite);
//            result = new RdfDataSourceWrapperWithRewrite<>(delegate, effectiveRewrite);
//        } else {
//            result = new RdfDataSourceWrapperWithRewrite<>(dataSource, rewrite);
//        }
//        return result;
//    }
//
//    /**
//     * Return a new data source that applies the given statement transform.
//     *
//     * If the given transform in an instance of {@link SparqlStmtTransformViaRewrite} then the rewrite is
//     * unwrapped and passed to {@link #wrapWithOpTransform(RDFDataSource, Rewrite)}.
//     *
//     * @param dataSource
//     * @param stmtTransform
//     * @return
//     */
//    public static RDFDataSource wrapWithStmtTransform(RDFDataSource dataSource, SparqlStmtTransform stmtTransform) {
//        RDFDataSource result;
//        if (stmtTransform instanceof SparqlStmtTransformViaRewrite) {
//            Rewrite rewrite = ((SparqlStmtTransformViaRewrite)stmtTransform).getRewrite();
//            result = wrapWithOpTransform(dataSource, rewrite);
//        } else {
//            result = wrapWithLinkTransform(dataSource,
//                link -> RDFLinkUtils.wrapWithStmtTransform(link, stmtTransform));
//        }
//        return result;
//    }
//
//    public static RDFDataSource wrapWithLinkTransform(RDFDataSource rdfDataSource, RDFLinkTransform linkXform) {
//        return new RDFDataSourceWrapperBase<>(rdfDataSource) {
//            @Override
//            public RDFConnection getConnection() {
//                RDFConnection base = super.getConnection();
//                RDFConnection r = RDFConnectionUtils.wrapWithLinkTransform(base, linkXform);
//                return r;
//            }
//        };
//    }
//
//    /**
//     * Wrap an RdfDataSource that any update link is wrapped.
//     */
//    public static RDFDataSource decorateUpdate(RDFDataSource dataSource, UpdateExecTransform updateExecTransform) {
//        LinkSparqlUpdateTransform componentTransform = LinkSparqlUpdateUtils.newTransform(updateExecTransform);
//        RDFDataSource result = wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
//        return result;
//    }
//
//    /**
//     * Wrap a LinkSparqlQuery such that a possible write action is run when txn.begin() is invoked.
//     * The action is run before the transaction is started.
//     * Can be used to update e.g. the spatial index before a read transaction.
//     */
//    public static RDFDataSource decorateQueryBeforeTxnBegin(RDFDataSource dataSource, Runnable action) {
//        LinkSparqlQueryTransform componentTransform = link -> new LinkSparqlQueryWrapperBase(link) {
//            @Override
//            public void begin(TxnType type) {
//                action.run();
//                super.begin(type);
//            }
//            @Override
//            public void begin(ReadWrite readWrite) {
//                begin(TxnType.convert(readWrite));
//            }
//        };
//        RDFDataSource result = wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
//        return result;
//    }

    /**
     * Create a {@link LinkSparqlQueryTransform} that intercepts construct query
     * requests and transforms them into select query ones.
     * The execution work is done in a {@link QueryExecBaseSelect}.
     */
//    public static LinkSparqlQueryTransform execQueryViaSelect(Predicate<Query> convertToSelect) {
//        LinkSparqlQueryTransform result = baseLink -> {
//            return new LinkSparqlQueryWrapperBase(baseLink) {
//                @Override
//                public QueryExecBuilder newQuery() {
//                    return new QueryExecBuilderWrapperBaseParse(baseLink.newQuery()) {
//                        protected Query seenQuery = null;
//                        @Override
//                        public QueryExecBuilder query(Query query) {
//                            seenQuery = query;
//                            return this;
//                        }
//                        @Override
//                        public QueryExec build() {
//                            Objects.requireNonNull(seenQuery, "Query not set");
//                            QueryExec r;
//                            boolean doConvert = convertToSelect.test(seenQuery);
//                            if (doConvert) {
//                                r = QueryExecSelect.of(seenQuery, q -> delegate.query(q).build());
//                            } else {
//                                r = getDelegate().query(seenQuery).build();
//                            }
//                            return r;
//                        }
//                    };
//                }
//            };
//        };
//        return result;
//    }

//    /**
//     * Wrap a data source such that matching queries are transformed to and executed as SELECT queries.
//     * The select query is passed on to the underlying data source, whereas the row set
//     * is post processed locally to fulfill the original request.
//     *
//     * @param dataSource The data source which to wrap
//     * @param convertToSelect Only matching queries are executed as select
//     * @return
//     */
//    public static RDFDataSource execQueryViaSelect(RDFDataSource dataSource, Predicate<Query> convertToSelect) {
//        LinkSparqlQueryTransform decorizer = execQueryViaSelect(convertToSelect);
//        RDFDataSource result = RdfDataSources.wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, decorizer));
//        return result;
//    }

//
//    /**
//     * If the dataset supports transactions then return a wrapped datasource that starts
//     * transactions when none is active.
//     * This method checks transaction support only immediately.
//     */
//    public static RDFDataSource wrapWithAutoTxn(RDFDataSource dataSource, Dataset dataset) {
//        RDFDataSource result = dataset.supportsTransactions()
//            ? RdfDataSources.wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.wrapWithAutoTxn(link, dataset))
//            : dataSource;
//        return result;
//    }
//
//    public static RDFDataSource withPagination(RDFDataSource dataSource, long pageSize) {
//        return new RdfDataSourceWithPagination(dataSource, pageSize);
//    }
//
//    public static RDFDataSource withLimit(RDFDataSource dataSource, long limit) {
//        return limit == Query.NOLIMIT
//            ? dataSource
//            : () -> RDFConnectionUtils.wrapWithQueryTransform(dataSource.getConnection(), q -> QueryUtils.restrictToLimit(q, limit, true));
//    }

//    public static RDFDataSource wrapWithLogging(RDFDataSource delegate) {
//        return new RDFDataSourceWrapperBase<>(delegate) {
//            protected AtomicInteger counter = new AtomicInteger();
//
//            @Override
//            public RDFConnection getConnection() {
//                RDFConnection base = super.getConnection();
//                return RDFConnectionUtils.wrapWithQueryTransform(base, null, qe ->
//                    new QueryExecWrapperBase<QueryExec>(qe) {
//                        @Override
//                        public void beforeExec() {
//                            int value = counter.incrementAndGet();
//                            if (logger.isInfoEnabled()) {
//                                logger.info("{}: Request #{}: {}", Thread.currentThread().getName(), value, getDelegate().getQueryString());
//                            }
//                        }
//                    });
//            }
//        };
//    }

//    /** This method backs {@link RDFDataSource#newQuery()}. */
//    public static QueryExecutionBuilder newQueryBuilder(RDFDataSource rdfDataSource) {
//        QueryExecBuilder queryExecBuilder = new QueryExecBuilderCustomBase<>() {
//            @Override
//            public QueryExec build() {
//                // Acquire a fresh connection.
//                RDFConnection conn = rdfDataSource.getConnection();
//                QueryExecBuilder delegateBuilder = QueryExecBuilderAdapter.adapt(conn.newQuery());
//
//                // Apply all settings to the delegate.
//                applySettings(delegateBuilder);
//
//                QueryExec delegateExec = delegateBuilder.build();
//
//                // Wrap the delegate execution such that closing it closes the underlying connection.
//                return new QueryExecWrapperBase<>(delegateExec) {
//                    @Override
//                    public void close() {
//                        super.close();
//                        conn.close();
//                    }
//                };
//            }
//        };
//        return QueryExecutionBuilderAdapter.adapt(queryExecBuilder);
//    }
//
//    public static UpdateExecutionBuilder newUpdateBuilder(RDFDataSource rdfDataSource) {
//        UpdateExecBuilder updateExecBuilder = new UpdateExecBuilderCustomBase<>() {
//            @Override
//            public UpdateExec build() {
//                Context cxt = this.getContext();
//
//                return new UpdateExec() {
//                    protected volatile UpdateExec delegate = null;
//                    protected volatile boolean isAborted = false;
//
//                    @Override
//                    public void abort() {
//                        if (!isAborted) {
//                            UpdateExec current = null;
//                            boolean doAbort = false;
//                            synchronized (this) {
//                                if (!isAborted) {
//                                    isAborted = true;
//                                    doAbort = true;
//                                    current = delegate;
//                                }
//                            }
//                            if (doAbort && current != null) {
//                                current.abort();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public Context getContext() {
//                        return cxt;
//                    }
//
//                    @Override
//                    public void execute() {
//                        // Wrap the delegate execution such that the underlying connection is closed upon
//                        // completing the execution
//
//                        RDFConnection conn = null;
//                        try {
//                            synchronized (this) {
//                                if (isAborted) {
//                                    throw new QueryCancelledException();
//                                }
//
//                                if (delegate != null) {
//                                    throw new RuntimeException("Execution already started.");
//                                }
//
//                                // Acquire a fresh connection.
//                                conn = rdfDataSource.getConnection();
//                                UpdateExecBuilder delegateBuilder = UpdateExecBuilderAdapter.adapt(conn.newUpdate());
//                                // Apply all settings to the delegate.
//                                applySettings(delegateBuilder);
//
//                                delegate = delegateBuilder.build();
//                            }
//
//                            delegate.execute();
//                        } finally {
//                            if (conn != null) {
//                                conn.close();
//                            }
//                        }
//                    }
//                };
//            }
//        };
//        return UpdateExecutionBuilderAdapter.adapt(updateExecBuilder);
//    }

//    public static Map<String, UserDefinedFunctionDefinition> loadMacros(String macroSource) {
//        Map<String, UserDefinedFunctionDefinition> udfRegistry = new LinkedHashMap<>();
//        loadMacros(Set.of(), udfRegistry, macroSource);
//        return udfRegistry;
//    }
//
//    /** Load macros from a given source w.r.t. the given profiles and add them to the registry. */
//    public static void loadMacros(Set<String> macroProfiles, Map<String, UserDefinedFunctionDefinition> udfRegistry, String macroSource) {
//        Model model = RDFDataMgr.loadModel(macroSource);
//        SparqlStmtMgr.execSparql(model, "udf-inferences.rq");
//        Map<String, UserDefinedFunctionDefinition> contrib = UserDefinedFunctions.load(model, macroProfiles);
//        udfRegistry.putAll(contrib);
//    }
//
//    /** Wrap a data source with query rewriting for macro expansion. */
//    public static RDFDataSource wrapWithMacros(RDFDataSource rdfDataSource, Map<String, UserDefinedFunctionDefinition> udfRegistry) {
//        ExprTransform eform = new ExprTransformPrettyMacroExpansion(udfRegistry);
//        RDFDataSource result = wrapWithExprTransform(rdfDataSource, eform);
//        return result;
//    }
//
//    /** Wrap a data source with query rewriting for macro expansion. */
//    public static RDFDataSource wrapWithExprTransform(RDFDataSource rdfDataSource, ExprTransform eform) {
//        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.ofExprTransform(eform);
//        RDFDataSource result = wrapWithStmtTransform(rdfDataSource, stmtTransform);
//        return result;
//    }

    public static RDFDataSource decorate(RDFDataSource base, RdfDataSourceTransform transform) {
        RDFDataSource result = transform.apply(base);
        return result;
    }

    public static RDFDataSource decorate(RDFDataSource base, RDFLinkTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, QueryTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, QueryExecTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, UpdateRequestTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, RDFLinkSourceTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, LinkSparqlQueryTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, SparqlStmtTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    public static RDFDataSource decorate(RDFDataSource base, Rewrite transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource decorate(RDFDataSource base, ExprTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(base, tmp);
    }

    public static RDFDataSource execQueryViaSelect(RDFDataSource base, Predicate<Query> query) {
        LinkSparqlQueryTransform tmp = RDFLinkSources.execQueryViaSelect(query);
        return decorate(base, tmp);
    }

    public static RDFDataSource wrapWithMacros(RDFDataSource base, Map<String, UserDefinedFunctionDefinition> udfRegistry) {
        return decorate(base, (RDFLinkSource linkSource) -> RDFLinkSources.wrapWithMacros(linkSource, udfRegistry));
    }
}
