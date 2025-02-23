package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.deleted.RDFLinkSourceWrapperWithSparqlStmtTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperBaseParse;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.ExprTransformPrettyMacroExpansion;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceWrapperWithRewrite;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransformPaginate;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateUpdateTransform;
import org.aksw.jenax.model.udf.util.UserDefinedFunctions;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransformViaRewrite;
import org.aksw.jenax.stmt.core.SparqlStmtTransforms;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFLinkSources {
    private static final Logger logger = LoggerFactory.getLogger(RDFLinkSources.class);

    public static RDFLinkSource of(Graph graph) {
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        return of(dsg);
    }

    public static RDFLinkSource of(DatasetGraph datasetGraph) {
        return new RDFLinkSourceOverDatasetGraph(datasetGraph);
    }

    /** This method backs {@link RDFLinkSource#newQuery()}. */
    public static QueryExecBuilder newQueryBuilder(RDFLinkSource linkSource) {
        QueryExecBuilder queryExecBuilder = new QueryExecBuilderCustomBase<>() {
            @Override
            public QueryExec build() {
                // Acquire a fresh connection.
                RDFLink link = linkSource.newLink();
                QueryExecBuilder delegateBuilder = link.newQuery();

                // Apply all settings to the delegate.
                applySettings(delegateBuilder);

                QueryExec delegateExec = delegateBuilder.build();

                // Wrap the delegate execution such that closing it closes the underlying connection.
                return new QueryExecWrapperBase<>(delegateExec) {
                    @Override
                    public void close() {
                        super.close();
                        link.close();
                    }
                };
            }
        };
        return queryExecBuilder;
    }

    /** This method backs {@link RDFLinkSource#newUpdadte()}. */
    public static UpdateExecBuilder newUpdateBuilder(RDFLinkSource linkSource) {
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

                        RDFLink link = null;
                        try {
                            synchronized (this) {
                                if (isAborted) {
                                    throw new QueryCancelledException();
                                }

                                if (delegate != null) {
                                    throw new RuntimeException("Execution already started.");
                                }

                                // Acquire a fresh connection.
                                link = linkSource.newLink();
                                UpdateExecBuilder delegateBuilder = link.newUpdate();
                                // Apply all settings to the delegate.
                                applySettings(delegateBuilder);

                                delegate = delegateBuilder.build();
                            }

                            delegate.execute();
                        } finally {
                            if (link != null) {
                                link.close();
                            }
                        }
                    }
                };
            }
        };
        return updateExecBuilder;
    }

    public static RDFLinkSource wrapWithLogging(RDFLinkSource delegate) {
        return new RDFLinkSourceWrapperBase<>(delegate) {
            protected AtomicInteger counter = new AtomicInteger();

            @Override
            public RDFLink newLink() {
                RDFLink base = super.newLink();
                return RDFLinkUtils.wrapWithQueryTransform(base, null, qe ->
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

    /**
     * Wrap an RdfDataSource that any update link is wrapped.
     */
//    public static RDFLinkSource decorateUpdate(RDFLinkSource linkSource, UpdateExecTransform updateExecTransform) {
//        return RDFLinkSourceDecorator.of(linkSource).decorate(updateExecTransform).build();
//
////        LinkSparqlUpdateTransform componentTransform = LinkSparqlUpdateUtils.newTransform(updateExecTransform);
////        RDFLinkSource result = () -> RDFLinkUtils.apply(linkSource.newLink(), componentTransform);
//        // return result;
//    }

    /**
     * Wrap a LinkSparqlQuery such that a possible write action is run when txn.begin() is invoked.
     * The action is run before the transaction is started.
     * Can be used to update e.g. the spatial index before a read transaction.
     */
    public static RDFLinkSource decorateQueryBeforeTxnBegin(RDFLinkSource linkSource, Runnable action) {
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
        RDFLinkSource result = wrapWithLinkTransform(linkSource, link -> RDFLinkUtils.apply(link, componentTransform));
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
    public static RDFLinkSource execQueryViaSelect(RDFLinkSource linkSource, Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform decorizer = execQueryViaSelect(convertToSelect);
        RDFLinkSource result = RDFLinkSourceDecorator.of(linkSource).decorate(decorizer).build();
        // RDFLinkSource result = () -> RDFLinkUtils.apply(linkSource.newLink(), decorizer);
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

    /**
     * If the dataset supports transactions then return a wrapped datasource that starts
     * transactions when none is active.
     * This method checks transaction support only immediately.
     */
    public static RDFLinkSource wrapWithAutoTxn(RDFLinkSource linkSource, Dataset dataset) {
        RDFLinkSource result = dataset.supportsTransactions()
            ? wrapWithLinkTransform(linkSource, link -> RDFLinkUtils.wrapWithAutoTxn(link, dataset))
            : linkSource;
        return result;
    }

//    public static RDFDataSource withPagination(RDFDataSource dataSource, long pageSize) {
//        return new RdfDataSourceWithPagination(dataSource, pageSize);
//    }

//    public static RDFLinkSource withLimit(RDFLinkSource linkSource, long limit) {
//        return limit == Query.NOLIMIT
//            ? linkSource
//            : () -> RDFLinkUtils.wrapWithQueryTransform(linkSource.newLink(), q -> QueryUtils.restrictToLimit(q, limit, true));
//    }

    /**
     * Returns a new data source that applies the given rewrite.
     *
     * In order to reduce Algebra.compile / OpAsQuery.asQuery round trips,
     * if the base data source is already a {@link RdfDataSourceWrapperWithRewrite} then
     * the rewrite is unwrapped and combined with the given rewrite.
     */
    public static RDFLinkSource wrapWithOpTransform(RDFLinkSource dataSource, Rewrite rewrite) {
        return RDFLinkSourceDecorator.of(dataSource).decorate((Op op) -> rewrite.rewrite(op)).build();

//        RDFLinkSource result;
//        if (dataSource instanceof RDFLinkSourceWrapperWithRewrite tmp) {
//            Rewrite before = tmp.getRewrite();
//            RDFLinkSource delegate = tmp.getDelegate();
//            // Rewrite effectiveRewrite = OpTransformList.flatten(true, before, rewrite);
//            OpTransform effectiveRewrite = TransformList.flatten(true, OpTransformList::new, before, rewrite);
//            result = new RDFLinkSourceWrapperWithRewrite<>(delegate, effectiveRewrite);
//        } else {
//            result = new RDFLinkSourceWrapperWithRewrite<>(dataSource, rewrite);
//        }
//        return result;
    }

    public static RDFLinkSource wrapWithLinkTransform(RDFLinkSource linkSource, RDFLinkTransform transform) {
        return new RDFLinkSourceWrapperWithLinkTransform<>(linkSource, transform);
    }

    /**
     * Return a new data source that applies the given statement transform.
     *
     * If the given transform in an instance of {@link SparqlStmtTransformViaRewrite} then the rewrite is
     * unwrapped and passed to {@link #wrapWithOpTransform(RDFLinkSource, Rewrite)}.
     *
     * @param linkSource
     * @param stmtTransform
     * @return
     */
    public static RDFLinkSource wrapWithStmtTransform(RDFLinkSource linkSource, SparqlStmtTransform stmtTransform) {
        RDFLinkSource result;
        if (stmtTransform instanceof SparqlStmtTransformViaRewrite) {
            Rewrite rewrite = ((SparqlStmtTransformViaRewrite)stmtTransform).getRewrite();
            result = wrapWithOpTransform(linkSource, rewrite);
        } else {
            result = new RDFLinkSourceWrapperWithSparqlStmtTransform<>(linkSource, stmtTransform);
        }
        return result;
    }

    /**
     * Wrap a datasource such that a (stateless) algebra transform is applied to each statement.
     */
    public static RDFLinkSource wrapWithOpTransform(RDFLinkSource dataSource, Transform statelessTransform) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(statelessTransform);
        return wrapWithStmtTransform(dataSource, stmtTransform);
    }

    public static RDFLinkSource wrapWithLinkSparqlQueryTransform(RDFLinkSource linkSource, LinkSparqlQueryTransform transform) {
        return wrapWithLinkTransform(linkSource, link -> RDFLinkUtils.wrapWithQueryLinkTransform(link, transform));
    }

    public static RDFLinkSource wrapWithLinkSparqlUpdateTransform(RDFLinkSource linkSource, LinkSparqlUpdateTransform transform) {
        return wrapWithLinkTransform(linkSource, link -> RDFLinkUtils.wrapWithUpdateLinkTransform(link, transform));
    }

   /**
    * Wrap a datasource such that an algebra transform is applied to each statement.
    */
   // XXX Improve method to flatten multiple stacking transforms on a dataSource into a
   //   list which makes debugging easier
   public static RDFLinkSource wrapWithOpTransform(RDFLinkSource dataSource, Supplier<Transform> transformSupplier) {
       SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(transformSupplier);
       return wrapWithStmtTransform(dataSource, stmtTransform);
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
    public static RDFLinkSource wrapWithMacros(RDFLinkSource linkSource, Map<String, UserDefinedFunctionDefinition> udfRegistry) {
        ExprTransform eform = new ExprTransformPrettyMacroExpansion(udfRegistry);
        RDFLinkSource result = wrapWithExprTransform(linkSource, eform);
        return result;
    }

    /** Wrap a data source with query rewriting for macro expansion. */
    public static RDFLinkSource wrapWithExprTransform(RDFLinkSource linkSource, ExprTransform eform) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.ofExprTransform(eform);
        RDFLinkSource result = wrapWithStmtTransform(linkSource, stmtTransform);
        return result;
    }

    public static RDFLinkSource wrapWithUpdateTransform(RDFLinkSource linkSource, UpdateRequestTransform updateTransform) {
        return wrapWithLinkSparqlUpdateTransform(linkSource,
            link -> new LinkSparqlUpdateUpdateTransform(link, updateTransform, null));
    }

    public static RDFLinkSource wrapWithQueryTransform(RDFLinkSource linkSource, QueryTransform queryTransform) {
        return wrapWithLinkSparqlQueryTransform(linkSource,
            link -> new LinkSparqlQueryQueryTransform(link,
                queryTransform, null));
    }

    public static RDFLinkSource wrapWithQueryExecTransform(RDFLinkSource linkSource, QueryExecTransform queryExecTransform) {
        return wrapWithLinkSparqlQueryTransform(linkSource,
            link -> new LinkSparqlQueryQueryTransform(link,
                null, queryExecTransform));
    }

    public static RDFLinkSource withLimit(RDFLinkSource linkSource, long limit) {
        return limit == Query.NOLIMIT
            ? linkSource
            : wrapWithQueryTransform(linkSource, q -> QueryUtils.restrictToLimit(q, limit, true));
    }

    public static RDFLinkSource withPaginate(RDFLinkSource linkSource, long pageSize) {
        return wrapWithLinkSparqlQueryTransform(linkSource, new LinkSparqlQueryTransformPaginate(pageSize));
    }
}
