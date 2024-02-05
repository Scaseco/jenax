package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rewrite that attempts to inject cache operations around group by computations.
 *
 * SERVICE <cache:> { SERVICE <env:REMOTE> { GROUP-BY } }
 */
public class RdfDataSourceWithLocalLateral
    extends RdfDataSourceWrapperBase<RdfDataSource>
{
    /** AutoDetector */
    public static class AutoDetector
        implements RdfDataSourceDecorator
    {
        protected Query probeQuery;

        public AutoDetector() {
            probeQuery = QueryFactory.create("SELECT * { ?s <urn:foo> <urn:bar> LATERAL { ?s <urn:foo> <urn:bar> } }");
        }

        public AutoDetector(Query probeQuery) {
            super();
            this.probeQuery = probeQuery;
        }

        // @Override
        public RdfDataSource decorate(RdfDataSource decoratee, Map<String, Object> options) {
            RdfDataSource result;
            try (QueryExecution qe =  decoratee.asQef().createQueryExecution(probeQuery)) {
                ResultSet rs = qe.execSelect();
                ResultSetFormatter.consume(rs);
                result = decoratee;
            } catch (QueryExecException e) {
                logger.info("Probing for LATERAL failed. Adding polyfill.");
                // TODO Improve distinguishing server error from unsupported LATERAL
                result = new RdfDataSourceWithLocalLateral(decoratee);
            }
            return result;
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

    public static final Node CACHE_NODE = NodeFactory.createURI("cache:");

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    public Dataset proxyDataset;

    // TODO Convert to test case
    public static void main(String[] args) {
        String queryStr = "SELECT (COUNT(*) AS ?c) { { SELECT ?s { ?s ?p ?o } LIMIT 1 } LATERAL { ?s ?x ?y } LATERAL { { SELECT * { ?s a ?foo } LIMIT 3 } } } GROUP BY ?s";

        // String queryStr = "SELECT (COUNT(*) AS ?c) { { SELECT ?s { ?s ?p ?o } LIMIT 1 } { ?s ?x ?y } { { SELECT * { ?s a ?foo } LIMIT 3 } } } GROUP BY ?s";


        Query query = QueryFactory.create(queryStr);

        // RdfDataSourceWithLocalCache.createProxyDataset(RdfDataEngines.of(DatasetFactory.create()))
        RdfDataSourceWithLocalLateral dataSource = RdfDataSourceWithLocalLateral.wrap(RdfDataEngines.of(DatasetFactory.create()));
        Query rewritten = OpRewriteInjectRemoteOps.rewriteQuery(query);
        System.out.println(rewritten);
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
        registry.addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().equals(REMOTE_NODE)) {
                RDFConnection base = delegate.getConnection();
                r = RDFConnectionUtils.execService(binding, execCxt, opExec, base);
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
