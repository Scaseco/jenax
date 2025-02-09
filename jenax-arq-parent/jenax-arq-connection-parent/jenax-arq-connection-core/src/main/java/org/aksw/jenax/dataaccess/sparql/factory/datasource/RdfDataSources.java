package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.arq.util.op.RewriteList;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperBaseParse;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateUtils;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithPagination;
import org.aksw.jenax.model.udf.util.UserDefinedFunctions;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransformViaRewrite;
import org.aksw.jenax.stmt.core.SparqlStmtTransforms;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;
import org.apache.jena.sparql.exec.QueryExecutionBuilderAdapter;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilderAdapter;
import org.apache.jena.sparql.exec.UpdateExecutionBuilderAdapter;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfDataSources {

    private static final Logger logger = LoggerFactory.getLogger(RdfDataSources.class);

    /**
     * Create a datasource where any attempt to <b>execute</b> a query or an update will fail.
     * @implNote The implementation internally decorates an RdfDataSource over a read-only empty dataset.
     */
    public static RdfDataSource alwaysFail() {
        // Using the remote builder defers parsing query strings until the execution
        // The builder for local dataset connections parses query eagerly.
        RdfDataSource dummy = () -> RDFConnectionRemote.newBuilder().destination("urn:dummy").parseCheckSPARQL(false).build();
        // RdfDataSource dummy = of(DatasetFactory.empty());
        RdfDataSource result = dummy.decorate(RdfDataSourceTransforms.alwaysFail());
        return result;
    }

    public static RdfDataSource of(Graph graph) {
        Model model = ModelFactory.createModelForGraph(graph);
        return of(model);
    }

    public static RdfDataSource of(Model model) {
        Dataset ds = DatasetFactory.wrap(model);
        return of(ds);
    }

    public static RdfDataSource of(DatasetGraph dsg) {
        Dataset ds = DatasetFactory.wrap(dsg);
        return of(ds);
    }

    public static RdfDataSource of(Dataset dataset) {
        return () -> RDFConnection.connect(dataset);
    }


    /**
     * Execute a query and invoke a function on the response.
     * Upon returning the internally freshly obtained connection and query execution are closed so
     * the result must be detached from those resources.
     */
    public static <T> T exec(RdfDataSource dataSource, Query query, Function<? super QueryExecution, T> qeToResult) {
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
    public static RdfDataEngine setupRdfDataSource(Map<String, Object> options) throws Exception {
        RdfDataSourceSpecBasicFromMap spec = RdfDataSourceSpecBasicFromMap.wrap(options);

        String sourceType = Optional.ofNullable(spec.getEngine()).orElse("mem");

        RdfDataEngineFactory factory = RdfDataEngineFactoryRegistry.get().getFactory(sourceType);
        if (factory == null) {
            throw new RuntimeException("No RdfDataSourceFactory registered under name " + sourceType);
        }

        RdfDataEngine result = factory.create(options);
        return result;
    }

    /**
     * Wrap a datasource such that a (stateless) algebra transform is applied to each statement.
     */
    public static RdfDataSource wrapWithOpTransform(RdfDataSource dataSource, Transform statelessTransform) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(statelessTransform);
        return wrapWithStmtTransform(dataSource, stmtTransform);
    }

    /**
     * Wrap a datasource such that an algebra transform is applied to each statement.
     */
    // XXX Improve method to flatten multiple stacking transforms on a dataSource into a
    //   list which makes debugging easier
    public static RdfDataSource wrapWithOpTransform(RdfDataSource dataSource, Supplier<Transform> transformSupplier) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(transformSupplier);
        return wrapWithStmtTransform(dataSource, stmtTransform);
    }

    /**
     * Returns a new data source that applies the given rewrite.
     *
     * In order to reduce Algebra.compile / OpAsQuery.asQuery round trips,
     * if the base data source is already a {@link RdfDataSourceWrapperWithRewrite} then
     * the rewrite is unwrapped and combined with the given rewrite.
     */
    public static RdfDataSource wrapWithOpTransform(RdfDataSource dataSource, Rewrite rewrite) {
        RdfDataSource result;
        if (dataSource instanceof RdfDataSourceWrapperWithRewrite) {
            RdfDataSourceWrapperWithRewrite<?> tmp = (RdfDataSourceWrapperWithRewrite<?>)dataSource;

            Rewrite before = tmp.getRewrite();
            RdfDataSource delegate = tmp.getDelegate();
            Rewrite effectiveRewrite = RewriteList.flatten(true, before, rewrite);
            result = new RdfDataSourceWrapperWithRewrite<>(delegate, effectiveRewrite);
        } else {
            result = new RdfDataSourceWrapperWithRewrite<>(dataSource, rewrite);
        }
        return result;
    }

    /**
     * Return a new data source that applies the given statement transform.
     *
     * If the given transform in an instance of {@link SparqlStmtTransformViaRewrite} then the rewrite is
     * unwrapped and passed to {@link #wrapWithOpTransform(RdfDataSource, Rewrite)}.
     *
     * @param dataSource
     * @param stmtTransform
     * @return
     */
    public static RdfDataSource wrapWithStmtTransform(RdfDataSource dataSource, SparqlStmtTransform stmtTransform) {
        RdfDataSource result;
        if (stmtTransform instanceof SparqlStmtTransformViaRewrite) {
            Rewrite rewrite = ((SparqlStmtTransformViaRewrite)stmtTransform).getRewrite();
            result = wrapWithOpTransform(dataSource, rewrite);
        } else {
            result = wrapWithLinkTransform(dataSource,
                link -> RDFLinkUtils.wrapWithStmtTransform(link, stmtTransform));
        }
        return result;
    }

    public static RdfDataSource wrapWithLinkTransform(RdfDataSource rdfDataSource, RDFLinkTransform linkXform) {
        return new RdfDataSourceWrapperBase<>(rdfDataSource) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection base = super.getConnection();
                RDFConnection r = RDFConnectionUtils.wrapWithLinkTransform(base, linkXform);
                return r;
            }
        };
    }

    /**
     * Wrap an RdfDataSource that any update link is wrapped.
     */
    public static RdfDataSource decorateUpdate(RdfDataSource dataSource, UpdateExecTransform updateExecTransform) {
        LinkSparqlUpdateTransform componentTransform = LinkSparqlUpdateUtils.newTransform(updateExecTransform);
        RdfDataSource result = wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
        return result;
    }

    /**
     * Wrap a LinkSparqlQuery such that a possible write action is run when txn.begin() is invoked.
     * The action is run before the transaction is started.
     * Can be used to update e.g. the spatial index before a read transaction.
     */
    public static RdfDataSource decorateQueryBeforeTxnBegin(RdfDataSource dataSource, Runnable action) {
        LinkSparqlQueryTransform componentTransform = link -> new LinkSparqlQueryWrapperBase(link) {
            @Override
            public void begin(TxnType type) {
                action.run();
                super.begin(type);
            }
            @Override
            public void begin(ReadWrite readWrite) {
                begin(TxnType.convert(readWrite));
            }
        };
        RdfDataSource result = wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
        return result;
    }

    /**
     * Create a {@link LinkSparqlQueryTransform} that intercepts construct query
     * requests and transforms them into select query ones.
     * The execution work is done in a {@link QueryExecBaseSelect}.
     */
    public static LinkSparqlQueryTransform execQueryViaSelect(Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform result = baseLink -> {
            return new LinkSparqlQueryWrapperBase(baseLink) {
                @Override
                public QueryExecBuilder newQuery() {
                    return new QueryExecBuilderWrapperBaseParse(baseLink.newQuery()) {
                        protected Query seenQuery = null;
                        @Override
                        public QueryExecBuilder query(Query query) {
                            seenQuery = query;
                            return this;
                        }
                        @Override
                        public QueryExec build() {
                            Objects.requireNonNull(seenQuery, "Query not set");
                            QueryExec r;
                            boolean doConvert = convertToSelect.test(seenQuery);
                            if (doConvert) {
                                r = QueryExecSelect.of(seenQuery, q -> delegate.query(q).build());
                            } else {
                                r = getDelegate().query(seenQuery).build();
                            }
                            return r;
                        }
                    };
                }
            };
        };
        return result;
    }

    /** Utility method that bridges an RdfDataSource to a function that operates on a RDFConnection. */
    public static <T> T compute(RdfDataSource dataSource, Function<RDFConnection, T> computation) {
        T result;
        try (RDFConnection conn = dataSource.getConnection()) {
            result = computation.apply(conn);
        }
        return result;
    }

    /**
     * Wrap a data source such that matching queries are transformed to and executed as SELECT queries.
     * The select query is passed on to the underlying data source, whereas the row set
     * is post processed locally to fulfill the original request.
     *
     * @param dataSource The data source which to wrap
     * @param convertToSelect Only matching queries are executed as select
     * @return
     */
    public static RdfDataSource execQueryViaSelect(RdfDataSource dataSource, Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform decorizer = execQueryViaSelect(convertToSelect);
        RdfDataSource result = RdfDataSources.wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, decorizer));
        return result;
    }

    public static String fetchDatasetHash(RdfDataSource dataSource) {
        String result = QueryExecutionFactories.fetchDatasetHash(dataSource.asQef());
        return result;
    }

    /**
     * If the dataset supports transactions then return a wrapped datasource that starts
     * transactions when none is active.
     * This method checks transaction support only immediately.
     */
    public static RdfDataSource wrapWithAutoTxn(RdfDataSource dataSource, Dataset dataset) {
        RdfDataSource result = dataset.supportsTransactions()
            ? RdfDataSources.wrapWithLinkTransform(dataSource, link -> RDFLinkUtils.wrapWithAutoTxn(link, dataset))
            : dataSource;
        return result;
    }

    public static RdfDataSource withPagination(RdfDataSource dataSource, long pageSize) {
        return new RdfDataSourceWithPagination(dataSource, pageSize);
    }

    public static RdfDataSource withLimit(RdfDataSource dataSource, long limit) {
        return limit == Query.NOLIMIT
            ? dataSource
            : () -> RDFConnectionUtils.wrapWithQueryTransform(dataSource.getConnection(), q -> QueryUtils.restrictToLimit(q, limit, true));
    }

    public static RdfDataSource wrapWithLogging(RdfDataSource delegate) {
        return new RdfDataSourceWrapperBase<>(delegate) {
            protected AtomicInteger counter = new AtomicInteger();

            @Override
            public RDFConnection getConnection() {
                RDFConnection base = super.getConnection();
                return RDFConnectionUtils.wrapWithQueryTransform(base, null, qe ->
                    new QueryExecWrapperBase<QueryExec>(qe) {
                        @Override
                        public void beforeExec() {
                            int value = counter.incrementAndGet();
                            if (logger.isInfoEnabled()) {
                                logger.info("{}: Request #{}: {}", Thread.currentThread().getName(), value, getDelegate().getQueryString());
                            }
                        }
                    });
            }
        };
    }

    /** This method backs {@link RdfDataSource#newQuery()}. */
    public static QueryExecutionBuilder newQueryBuilder(RdfDataSource rdfDataSource) {
        QueryExecBuilder queryExecBuilder = new QueryExecBuilderCustomBase<>() {
            @Override
            public QueryExec build() {
                // Acquire a fresh connection.
                RDFConnection conn = rdfDataSource.getConnection();
                QueryExecBuilder delegateBuilder = QueryExecBuilderAdapter.adapt(conn.newQuery());

                // Apply all settings to the delegate.
                applySettings(delegateBuilder);

                QueryExec delegateExec = delegateBuilder.build();

                // Wrap the delegate execution such that closing it closes the underlying connection.
                return new QueryExecWrapperBase<>(delegateExec) {
                    @Override
                    public void close() {
                        super.close();
                        conn.close();
                    }
                };
            }
        };
        return QueryExecutionBuilderAdapter.adapt(queryExecBuilder);
    }

    public static UpdateExecutionBuilder newUpdateBuilder(RdfDataSource rdfDataSource) {
        UpdateExecBuilder updateExecBuilder = new UpdateExecBuilderCustomBase<>() {
            @Override
            public UpdateExec build() {
                Context cxt = this.getContext();

                return new UpdateExec() {
                    protected volatile UpdateExec delegate = null;
                    protected volatile boolean isAborted = false;

                    @Override
                    public void abort() {
                        if (!isAborted) {
                            UpdateExec current = null;
                            boolean doAbort = false;
                            synchronized (this) {
                                if (!isAborted) {
                                    isAborted = true;
                                    doAbort = true;
                                    current = delegate;
                                }
                            }
                            if (doAbort && current != null) {
                                current.abort();
                            }
                        }
                    }

                    @Override
                    public Context getContext() {
                        return cxt;
                    }

                    @Override
                    public void execute() {
                        // Wrap the delegate execution such that the underlying connection is closed upon
                        // completing the execution

                        RDFConnection conn = null;
                        try {
                            synchronized (this) {
                                if (isAborted) {
                                    throw new QueryCancelledException();
                                }

                                if (delegate != null) {
                                    throw new RuntimeException("Execution already started.");
                                }

                                // Acquire a fresh connection.
                                conn = rdfDataSource.getConnection();
                                UpdateExecBuilder delegateBuilder = UpdateExecBuilderAdapter.adapt(conn.newUpdate());
                                // Apply all settings to the delegate.
                                applySettings(delegateBuilder);

                                delegate = delegateBuilder.build();
                            }

                            delegate.execute();
                        } finally {
                            if (conn != null) {
                                conn.close();
                            }
                        }
                    }
                };
            }
        };
        return UpdateExecutionBuilderAdapter.adapt(updateExecBuilder);
    }

    public static Map<String, UserDefinedFunctionDefinition> loadMacros(String macroSource) {
        Map<String, UserDefinedFunctionDefinition> udfRegistry = new LinkedHashMap<>();
        loadMacros(Set.of(), udfRegistry, macroSource);
        return udfRegistry;
    }

    /** Load macros from a given source w.r.t. the given profiles and add them to the registry. */
    public static void loadMacros(Set<String> macroProfiles, Map<String, UserDefinedFunctionDefinition> udfRegistry, String macroSource) {
        Model model = RDFDataMgr.loadModel(macroSource);
        SparqlStmtMgr.execSparql(model, "udf-inferences.rq");
        Map<String, UserDefinedFunctionDefinition> contrib = UserDefinedFunctions.load(model, macroProfiles);
        udfRegistry.putAll(contrib);
    }

    /** Wrap a data source with query rewriting for macro expansion. */
    public static RdfDataSource wrapWithMacros(RdfDataSource rdfDataSource, Map<String, UserDefinedFunctionDefinition> udfRegistry) {
        // ExprTransform eform = new ExprTransformExpand(udfRegistry);
        ExprTransform eform = new ExprTransformCopy() {
            @Override
            public Expr transform(ExprFunctionN func, ExprList args) {
                // XXX Could avoid func.copy()
                return UserDefinedFunctions.expandMacro(udfRegistry, func.copy(args));
            }
        };
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.ofExprTransform(eform);
        RdfDataSource result = wrapWithStmtTransform(rdfDataSource, stmtTransform);
        return result;
    }
}
