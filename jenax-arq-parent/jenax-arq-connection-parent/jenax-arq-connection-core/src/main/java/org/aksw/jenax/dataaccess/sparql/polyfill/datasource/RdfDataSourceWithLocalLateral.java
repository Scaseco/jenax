package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.transform.TransformAssignToExtend;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;
import org.aksw.jenax.sparql.algebra.transform2.Evaluation;
import org.aksw.jenax.sparql.algebra.transform2.EvaluationCopy;
import org.aksw.jenax.sparql.algebra.transform2.Evaluator;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.util.backport.syntaxtransform.ElementTransformer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCloseable;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkConcurrent;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

/**
 * A rewrite that attempts to inject cache operations around group by computations.
 *
 * SERVICE <cache:> { SERVICE <env:REMOTE> { GROUP-BY } }
 */
public class RdfDataSourceWithLocalLateral
    extends RdfDataSourceWrapperBase<RdfDataSource>
{
    public record PolyfillLateralConfig(int bulkSize, int concurrentSlots) {
        /** Parse the settings of format [{bulkSize}[-{concurrentSlotCount}]]. */
        public static PolyfillLateralConfig parse(String val) {
            int bulkSize = 10;
            int concurrentSlots = 0;

            String v = val == null ? "" : val.toLowerCase().trim();
            if (!v.isEmpty()) {
                String[] parts = v.split("-", 2);
                if (parts.length > 0) {
                    bulkSize = parseInt(parts[0].trim(), 10);
                    Preconditions.checkArgument(bulkSize > 0, "Bulk size must be greater than 0.");

                    if (parts.length > 1) {
                        concurrentSlots = parseInt(parts[1].trim(), 0);
                        Preconditions.checkArgument(bulkSize > 0, "Concurrent slots must be greater than or equal to 0.");
                    }
                }
            }

            return new PolyfillLateralConfig(bulkSize, concurrentSlots);
        }
    }

    private static int parseInt(String str, int fallbackValue) {
        return str.isEmpty() ? fallbackValue : Integer.parseInt(str);
    }

    public static Node createServiceIri(int bulkSize, int concurrentSlots) {
        return NodeFactory.createURI(String.format("loop+scoped:concurrent+%d-%d:bulk+%d:", concurrentSlots, bulkSize, bulkSize));
    }

    public static class TransformLateralToBulk
        extends TransformCopy
    {
        protected Node serviceIri;

        public TransformLateralToBulk(int bulkSize, int concurrentSlots) {
            Preconditions.checkArgument(bulkSize > 0, "Bulk size must be at least 1.");
            Preconditions.checkArgument(concurrentSlots >= 0, "Number of concurrent slots must not be less than 0.");
            this.serviceIri = createServiceIri(bulkSize, concurrentSlots);
        }

        protected Node createServiceIri(int bulkSize, int concurrentSlots) {
            return RdfDataSourceWithLocalLateral.createServiceIri(bulkSize, concurrentSlots);
        }

        @Override
        public Op transform(OpLateral opLateral, Op left, Op right) {
            return OpSequence.create(left, new OpService(serviceIri, right, false));
        }
    }

    public static class ElementTransformLateralToBulk
        extends ElementTransformCopyBase
    {
        protected Node serviceIri;

        public ElementTransformLateralToBulk(PolyfillLateralConfig config) {
            this(config.bulkSize(), config.concurrentSlots());
        }

        public ElementTransformLateralToBulk(int bulkSize, int concurrentSlots) {
            Preconditions.checkArgument(bulkSize > 0, "Bulk size must be at least 1.");
            Preconditions.checkArgument(concurrentSlots >= 0, "Number of concurrent slots must not be less than 0.");
            this.serviceIri = createServiceIri(bulkSize, concurrentSlots);
        }

        protected Node createServiceIri(int bulkSize, int concurrentSlots) {
            return RdfDataSourceWithLocalLateral.createServiceIri(bulkSize, concurrentSlots);
        }

        @Override
        public Element transform(ElementLateral el, Element elt1) {
            return new ElementService(serviceIri, elt1, false);
        }
    }

    /** Factory class */
    public static class Factory
        implements RdfDataSourceDecorator
    {
        @Override
        public RdfDataSource decorate(RdfDataSource decoratee, Map<String, Object> options) {
            // TODO Extract options
            return RdfDataSourceWithLocalLateral.wrap(decoratee, null);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithLocalLateral.class);

    public static final String REMOTE_IRI = "env://REMOTE";
    public static final Node REMOTE_NODE = NodeFactory.createURI(REMOTE_IRI);

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    protected Dataset proxyDataset;
    protected PolyfillLateralConfig config;


    public static void main(String[] args) {
        Query a = QueryFactory.create("""
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            PREFIX lgdo: <http://linkedgeodata.org/ontology/>
            SELECT * {
              { SELECT # DISTINCT
                  ?t
                {
                  ?t a ?c .
                  # FILTER (?c IN (rdfs:Class, owl:Class))
                  # FILTER(isURI(?t)) FILTER(STRSTARTS(STR(?t), STR(lgdo:)))
                }
                #ORDER BY ?t
                LIMIT 5
              }
              LATERAL {
                ?t a ?c
                FILTER NOT EXISTS { SELECT * { ?s a ?t } LIMIT 2 }
                # { SELECT * { ?s a ?t } LIMIT 2 }
              }
            }
            """);
        Query b = OpRewriteInjectRemoteOps.rewriteQuery(a);
        System.out.println(b);
    }

    // TODO Convert to test case
    public static void mainX(String[] args) {
        for (int i= 0; i < 10; ++i) {
            mainActual(args);
        }
    }

    public static void mainActual(String[] args) {
        // String queryStr = "SELECT (COUNT(*) AS ?c) { { SELECT ?s { ?s ?p ?o } LIMIT 1 } LATERAL { ?s ?x ?y } LATERAL { { SELECT * { ?s a ?foo } LIMIT 3 } } } GROUP BY ?s";

        // String queryStr = "SELECT (COUNT(*) AS ?c) { { SELECT ?s { ?s ?p ?o } LIMIT 1 } { ?s ?x ?y } { { SELECT * { ?s a ?foo } LIMIT 3 } } } GROUP BY ?s";

        // GRAPH ?g { ?s a dcat:Dataset . ?s owl:sameAs ?v . ?v a void:Dataset . ?v void:classPartition ?vcp . ?vcp void:propertyPartition ?vcppp }
        String queryStr = """
            PREFIX dcat: <http://www.w3.org/ns/dcat#>
            PREFIX void: <http://rdfs.org/ns/void#>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            SELECT * {
              { SELECT * { GRAPH ?g { ?s a dcat:Dataset } } LIMIT 100 }
              LATERAL {
                { SELECT * { GRAPH ?g { ?s a dcat:Dataset . ?s owl:sameAs ?v . ?v a void:Dataset . ?v void:classPartition ?vcp } } LIMIT 10 }
                # LATERAL { SELECT ?g ?vcp (COUNT(*) AS ?ppCount) { GRAPH ?g { ?vcp void:propertyPartition ?vcppp } } GROUP BY ?g ?vcp}
              }
            }
            """;

        queryStr = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            SELECT * {
              { SELECT DISTINCT ?t { ?t a ?c . FILTER (?c IN (rdfs:Class, owl:Class)) FILTER(isURI(?t)) } LIMIT 100 }
              LATERAL {
                { SELECT * { ?s a ?t } LIMIT 2 }
              }
            }
            """;

        int[] counter = {0};
//        ServiceExecutorRegistry.get().addBulkLink((opService, input, execCxt, chain) -> {
//            // if (opService.getService().equals(REMOTE_NODE)) {
//                ++counter[0];
//            // }
//            return chain.createExecution(opService, input, execCxt);
//        });

        Query query = QueryFactory.create(queryStr);

        // RdfDataSourceWithLocalCache.createProxyDataset(RdfDataEngines.of(DatasetFactory.create()))
        // RdfDataSource coreX = () -> RDFConnection.connect("http://maven.aksw.org/sparql");
        RdfDataSource coreX = () -> RDFConnection.connect("http://linkedgeodata.org/sparql");
        RdfDataSource core = () -> RDFConnectionUtils.wrapWithQueryTransform(coreX.getConnection(), null, qe -> new QueryExecWrapperBase<QueryExec>(qe) {
            @Override
            public void beforeExec() {
                ++counter[0];
                System.err.println(Thread.currentThread().getName() + ": Request #" + counter[0] + ": " + getDelegate().getQueryString());
            }
        });


        RdfDataSourceWithLocalLateral dataSource = RdfDataSourceWithLocalLateral.wrap(core, new PolyfillLateralConfig(10, 10));
        Query rewritten = OpRewriteInjectRemoteOps.rewriteQuery(query);

        // QueryUtils.applyElementTransform(query, new ElementTransformLateralToBulk(10, 10));

        // rewritten = QueryTransformOps.transform(rewritten, new ElementTransformLateralToBulk(10, 10));
        // System.out.println(rewritten);

        int resultSetSize;
        Stopwatch sw = Stopwatch.createStarted();
        try (RDFConnection conn = dataSource.getConnection()) {
            try (QueryExecution qe = conn.query(rewritten)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetRewindable rs = qe.execSelect().rewindable();
                resultSetSize = rs.size();
                rs.reset();
                ResultSetFormatter.outputAsJSON(rs);
            }
        }

        System.out.println("Done - Remote requests: " + counter[0] + " - resultSetSize: " + resultSetSize + " - time: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }

    public RdfDataSourceWithLocalLateral(RdfDataSource delegate, PolyfillLateralConfig config) {
        super(delegate);
        this.proxyDataset = createProxyDataset(delegate);
        this.config = config;
    }

    public static RdfDataSourceWithLocalLateral wrap(RdfDataSource delegate) {
        return new RdfDataSourceWithLocalLateral(delegate, null);
    }

    public static RdfDataSourceWithLocalLateral wrap(RdfDataSource delegate, PolyfillLateralConfig config) {
        return new RdfDataSourceWithLocalLateral(delegate, config);
    }

    public static Dataset createProxyDataset(RdfDataSource delegate) {
        Dataset result = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.getBulkChain().add(new ChainingServiceExecutorBulkConcurrent());
        registry.getBulkChain().add(new ChainingServiceExecutorBulkServiceEnhancer());
        ServiceEnhancerInit.registerServiceExecutorSelf(registry);
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().equals(REMOTE_NODE)) {
                // Transform assigns to extend - assign ops may be injected by Jena v5.0.0 QueryIterLateral
                OpService finalOp = (OpService)Transformer.transform(TransformAssignToExtend.get(), opExec);
                // TODO Evaluate table-based algebra locally.
                RDFConnection base = delegate.getConnection();
                r = RDFConnectionUtils.execService(binding, execCxt, finalOp, base, true, true);
                r = new QueryIteratorCloseable(r, base::close);
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        });
        ServiceExecutorRegistry.set(result.getContext(), registry);
        return result;
    }

    protected SparqlStmt rewriteStatement(SparqlStmt stmt) {
        SparqlStmt result;
        if (stmt.isQuery()) {
            Query a = stmt.getQuery();
            Query b = OpRewriteInjectRemoteOps.rewriteQuery(a);
            Query c = config == null
                ? b
                : QueryUtils.applyElementTransform(b, elt -> ElementTransformer.transform(elt, new ElementTransformLateralToBulk(config)));
            result = new SparqlStmtQuery(c);
        } else  {
            result = stmt;
        }
        return result;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection base = RDFConnection.connect(proxyDataset);
        // TODO Properly generalize to stmts / update requests
        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base,
                q -> rewriteStatement(new SparqlStmtQuery(q)).getQuery(),
                qe -> {
                    ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                    return qe;
                });
        return result;
    }

    public static Op wrapWithRemote(Op op) {
        Op result = new OpService(REMOTE_NODE, op, false);
        return result;
    }


    // TODO Adapt to evaluation
    /**
     * A copying transform that applies an ElementTransform syntax pattern of
     * E_Exist and E_NoExists
     * */
    public class ExprTransformApplyElementTransform extends ExprTransformCopy
    {
        private final ElementTransform transform;

        public ExprTransformApplyElementTransform(ElementTransform transform) {
            this(transform, false);
        }

        public ExprTransformApplyElementTransform(ElementTransform transform, boolean alwaysDuplicate) {
            super(alwaysDuplicate);
            this.transform = transform;
        }

        @Override
        public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
        {
            Element el2 = ElementTransformer.transform(funcOp.getElement(), transform, this);

            if ( el2 == funcOp.getElement() )
                return super.transform(funcOp, args, opArg);
            if ( funcOp instanceof E_Exists )
                return new E_Exists(el2);
            if ( funcOp instanceof E_NotExists )
                return new E_NotExists(el2);
            throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp);
        }
    }

    /** Rewriter that injects caching operations after group by operations
     *
     * (Op, true) means that op contains some rewrite that references remote
     */
    public static class OpRewriteInjectRemoteOps
        implements EvaluationCopy<Entry<Op, Boolean>>
    {
        public static Query rewriteQuery(Query query) {
            Evaluation<Entry<Op, Boolean>> xform = new OpRewriteInjectRemoteOps();

            Query result = QueryUtils.applyOpTransform(query, op -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("LocalLateral - OriginalQuery:\n" + query);
                }

                Entry<Op, Boolean> tmp = Evaluator.evaluateSkipService(xform, op);

                Op r = Boolean.FALSE.equals(tmp.getValue())
                    ? wrapWithRemote(op)
                    : tmp.getKey();

                if (logger.isDebugEnabled()) {
                    logger.debug("LocalLateral - RewrittenQuery:\n" + query);
                }

                return r;
            });
            return result;
        }

        @Override
        public Entry<Op, Boolean> eval(OpFilter op, Entry<Op, Boolean> subOp) {

            return EvaluationCopy.super.eval(op, subOp);
        }

        @Override
        public Entry<Op, Boolean> eval(OpService op, Entry<Op, Boolean> subOp) {
            return Map.entry(op, true);
        }

        @Override
        public Entry<Op, Boolean> eval(OpLateral op, Entry<Op, Boolean> left, Entry<Op, Boolean> right) {
            Entry<Op, Boolean> result = evalAny(op, Arrays.asList(left, right), true);
            return result;
        }

        @Override
        public Entry<Op, Boolean> evalAny(Op op, List<Entry<Op, Boolean>> args) {
            Entry<Op, Boolean> result = evalAny(op, args, false);
            return result;
        }

        public Entry<Op, Boolean> evalAny(Op op, List<Entry<Op, Boolean>> args, boolean forceAllRemote) {
            // If all is remote
            Set<Boolean> localAndRemote = args.stream().map(Entry::getValue).distinct().collect(Collectors.toSet());
            boolean anyRemote = localAndRemote.contains(Boolean.TRUE);
            // boolean allRemote = localAndRemote.equals(Boolean.TRUE);

            Entry<Op, Boolean> result;
            if (forceAllRemote || anyRemote) {
                // Wrap all operations with remote
                List<Op> newArgs = args.stream().map(arg -> {
                    Op subOp = arg.getKey();
                    Op r = arg.getValue().booleanValue() ? subOp : wrapWithRemote(subOp);
                    return r;
                }).collect(Collectors.toList());
                result = Map.entry(OpUtils.copy(op, newArgs), true);
            } else {
                List<Op> newArgs = args.stream().map(Entry::getKey).collect(Collectors.toList());
                result = Map.entry(OpUtils.copy(op, newArgs), false);
            }

            return result;
        }

        // public static List<Op> forceAllRemote()
    }
}
