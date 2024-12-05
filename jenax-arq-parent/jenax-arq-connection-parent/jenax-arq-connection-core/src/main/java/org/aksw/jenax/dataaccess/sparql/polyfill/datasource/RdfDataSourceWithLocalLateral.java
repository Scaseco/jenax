package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.transform.TransformAssignToExtend;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;
import org.aksw.jenax.sparql.algebra.transform2.Evaluation;
import org.aksw.jenax.sparql.algebra.transform2.EvaluationCopy;
import org.aksw.jenax.sparql.algebra.transform2.Evaluator;
import org.aksw.jenax.util.backport.syntaxtransform.QueryTransformOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkConcurrent;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A rewrite that attempts to inject cache operations around group by computations.
 *
 * SERVICE <cache:> { SERVICE <env:REMOTE> { GROUP-BY } }
 */
public class RdfDataSourceWithLocalLateral
    extends RdfDataSourceWrapperBase<RdfDataSource>
{

    public static Node createServiceIri(int concurrentSlots, int bulkSize) {
        return NodeFactory.createURI(String.format("loop+scoped:concurrent+%d-%d:bulk+%d:", concurrentSlots, bulkSize, bulkSize));
    }

    public static class TransformLateralToBulk
        extends TransformCopy
    {
        protected Node serviceIri;

        public TransformLateralToBulk(int concurrentSlots, int bulkSize) {
            Preconditions.checkArgument(concurrentSlots >= 0, "Number of concurrent slots must not be less than 0.");
            Preconditions.checkArgument(bulkSize > 0, "Bulk size must be at least 1.");
            serviceIri = createServiceIri(concurrentSlots, bulkSize);
        }

        protected Node createServiceIri(int concurrentSlots, int bulkSize) {
            return RdfDataSourceWithLocalLateral.createServiceIri(concurrentSlots, bulkSize);
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

        public ElementTransformLateralToBulk(int concurrentSlots, int bulkSize) {
            Preconditions.checkArgument(concurrentSlots >= 0, "Number of concurrent slots must not be less than 0.");
            Preconditions.checkArgument(bulkSize > 0, "Bulk size must be at least 1.");
            serviceIri = createServiceIri(concurrentSlots, bulkSize);
        }

        protected Node createServiceIri(int concurrentSlots, int bulkSize) {
            return RdfDataSourceWithLocalLateral.createServiceIri(concurrentSlots, bulkSize);
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
            return new RdfDataSourceWithLocalLateral(decoratee);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithLocalLateral.class);

    public static final String REMOTE_IRI = "env://REMOTE";
    public static final Node REMOTE_NODE = NodeFactory.createURI(REMOTE_IRI);

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    public Dataset proxyDataset;

    // TODO Convert to test case
    public static void main(String[] args) {
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
                LATERAL { SELECT ?g ?vcp (COUNT(*) AS ?ppCount) { GRAPH ?g { ?vcp void:propertyPartition ?vcppp } } GROUP BY ?g ?vcp}
              }
            }
            """;


        Query query = QueryFactory.create(queryStr);

        // RdfDataSourceWithLocalCache.createProxyDataset(RdfDataEngines.of(DatasetFactory.create()))
        RdfDataSource core = () -> RDFConnection.connect("http://maven.aksw.org/sparql");
        RdfDataSourceWithLocalLateral dataSource = RdfDataSourceWithLocalLateral.wrap(core);
        Query rewritten = OpRewriteInjectRemoteOps.rewriteQuery(query);

        rewritten = QueryTransformOps.transform(rewritten, new ElementTransformLateralToBulk(10, 10));
        System.out.println(rewritten);

        try (RDFConnection conn = dataSource.getConnection()) {
            try (QueryExecution qe = conn.query(rewritten)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());

                ResultSetFormatter.outputAsJSON(qe.execSelect());
            }
        }

        System.out.println("Done.");
    }

    public RdfDataSourceWithLocalLateral(RdfDataSource delegate) {
        super(delegate);
        proxyDataset = createProxyDataset(delegate);
    }

    public static RdfDataSourceWithLocalLateral wrap(RdfDataSource delegate) {
        return new RdfDataSourceWithLocalLateral(delegate);
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
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        });
        ServiceExecutorRegistry.set(result.getContext(), registry);
        return result;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection base = RDFConnection.connect(proxyDataset);
        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base, OpRewriteInjectRemoteOps::rewriteQuery);
        return result;
    }

    public static Op wrapWithRemote(Op op) {
        Op result = new OpService(REMOTE_NODE, op, false);
        return result;
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
