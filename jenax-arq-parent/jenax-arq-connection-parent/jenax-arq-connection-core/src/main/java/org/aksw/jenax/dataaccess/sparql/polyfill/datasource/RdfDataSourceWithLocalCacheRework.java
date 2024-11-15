package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.algebra.transform.ProjectExtend;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.sparql.algebra.topdown.OpRewriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResponseCache;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rewrite that attempts to inject cache operations around group by computations.
 *
 * SERVICE <cache:> { SERVICE <env:REMOTE> { GROUP-BY } }
 */
// Currently not functional - the goal is to improve handling of filter expressions
public class RdfDataSourceWithLocalCacheRework
    extends RdfDataSourceWrapperBase<RdfDataSource>
{
    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithLocalCacheRework.class);

    public static final String REMOTE_IRI = "env://REMOTE";
    public static final Node REMOTE_NODE = NodeFactory.createURI(REMOTE_IRI);
    public static final Node CACHE_NODE = NodeFactory.createURI("cache:");

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    public Dataset proxyDataset;

    // TODO Convert to test case
    public static void main(String[] args) {
        String queryStr = "SELECT DISTINCT  ?s ?conditionId_1\n"
        + "WHERE\n"
        + "  { SELECT  ?s (MIN(str(?o)) AS ?sortKey_1)\n"
        + "    WHERE\n"
        + "      { ?s  ?p  ?o\n"
        + "        FILTER ( ?s IN (<bnode://genid_8777926670064186908_1004>, <urn:view_waynodes>) )\n"
        + "      }\n"
        + "    GROUP BY ?s\n"
        + "    ORDER BY ASC(MIN(str(?o)))\n"
        + "  }\n"
        + "ORDER BY ASC(?sortKey_1) ?s";

        queryStr = """
            SELECT DISTINCT  ?p ?o ?c
WHERE
  { SELECT DISTINCT  ?p ?o (COUNT(DISTINCT ?s) AS ?c)
    WHERE
      { ?s  a   <http://fp7-pp.publicdata.eu/ontology/Project> ;
            ?p  ?o
      }
    GROUP BY ?p ?o
    HAVING ( ?p = <http://fp7-pp.publicdata.eu/ontology/strategicObjective> )
  }
ORDER BY ASC(?p) ASC(?o)
LIMIT   127
        """;

        queryStr = """
            SELECT *
WHERE
  {{ SELECT DISTINCT  ?p ?o
    WHERE
      { SELECT DISTINCT  ?p ?o (COUNT(DISTINCT ?s) AS ?c)
        WHERE
          { ?s  a   <http://fp7-pp.publicdata.eu/ontology/Project> ;
                ?p  ?o
          }
        GROUP BY ?p ?o
      } }
      FILTER ( ?p = <http://fp7-pp.publicdata.eu/ontology/strategicObjective> )
  }
        """;

        Query query = QueryFactory.create(queryStr);

        Op op = Algebra.compile(query);
        // Rewrite rewrite = AlgebraUtils.createDefaultRewriter();
        Rewrite rewrite = x -> Transformer.transform(new TransformFilterPlacement(), x);
        op = rewrite.rewrite(op);

        System.out.println(op);

        // RdfDataSourceWithLocalCache.createProxyDataset(RdfDataEngines.of(DatasetFactory.create()))
        RdfDataSourceWithLocalCacheRework dataSource = new RdfDataSourceWithLocalCacheRework(RdfDataEngines.of(DatasetFactory.create()));
        Query rewritten = OpRewriteInjectCacheOps.rewriteQuery(query);
        System.out.println(rewritten);
    }

    public RdfDataSourceWithLocalCacheRework(RdfDataSource delegate) {
        super(delegate);
        proxyDataset = createProxyDataset(delegate);
    }

    /**
     * Create an (empty) dataset with a special ServiceExecutorRegistry that handles
     * caching and remote requests.
     *
     * @param delegate
     * @return
     */
    public static Dataset createProxyDataset(RdfDataSource delegate) {
        Dataset result = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());

        // Create a cache local to the dataset - don't use the global cache, because
        // all service requests will go to REMOTE_NODE
        ServiceResponseCache.set(result.getContext(), new ServiceResponseCache());

        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().equals(REMOTE_NODE)) {
                RDFConnection base = delegate.getConnection();
                r = RDFConnectionUtils.execService(binding, execCxt, opExec, base, true, true);
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
        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base, OpRewriteInjectCacheOps::rewriteQuery);
        // RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base, TransformInjectCacheOps::rewriteQuery);
        return result;
    }

    public static Entry<Op, Boolean> wrapWithCache(Op op) {
        Entry<Op, Boolean> result = Map.entry(wrapWithCacheOp(op), true);
        return result;
    }

    public static Op wrapWithCacheOp(Op op) {
        Op remote = wrapWithRemote(op);
        Op result = new OpService(CACHE_NODE, remote, false);
        return result;
    }

    public static Op wrapWithRemote(Op op) {
        Op result = new OpService(REMOTE_NODE, op, false);
        return result;
    }

    public static Op unwrapIfCached(Op op) {
        Op result = ObjectUtils.tryCastAs(OpService.class, op)
            .filter(x -> CACHE_NODE.equals(x.getService()))
            .map(OpService::getSubOp)
            .flatMap(subOp -> ObjectUtils.tryCastAs(OpService.class, subOp))
            .filter(x -> REMOTE_NODE.equals(x.getService()))
            .map(OpService::getSubOp)
            .orElse(null);
        return result;
    }


    /**
     * Rewriter that injects cache ops as the parent of group by ops.
     * Note, that all group by results are pulled into the client.
     */
    public static class OpRewriteInjectCacheOps
        implements OpRewriter<Entry<Op, Boolean>> {

        public static Query rewriteQuery(Query query) {
            if (logger.isDebugEnabled()) {
                logger.debug("OriginalQuery:\n" + query);
            }
            Query result = QueryUtils.applyOpTransform(query, OpRewriteInjectCacheOps::rewriteOpx);
            return result;
        }

        public static Op rewriteOpx(Op op) {
            OpRewriteInjectCacheOps rewriter = new OpRewriteInjectCacheOps();
            Entry<Op, Boolean> e = rewriter.rewriteOp(op);

            Op rr = e.getValue()
                    ? e.getKey()
                    : wrapWithRemote(op); // wrapWithCache(op).getKey(); - always cache?

            if (logger.isDebugEnabled()) {
                logger.debug("Cache rewrite [pushed=" + e.getValue() + "]: " + OpAsQuery.asQuery(rr));
            }
            return rr;
        }

        public Entry<Op, Boolean> handleOp1(Op1 op, Function<Op, Op> ctor) {
            Op subOp = op.getSubOp();
            Entry<Op, Boolean> tmp = rewriteOp(subOp);
            Op newSubOp = tmp.getKey();
            boolean rewritten = tmp.getValue();
            Entry<Op, Boolean> result = rewritten
                    ? Map.entry(ctor.apply(newSubOp), true)
                    : Map.entry(op, false);
            return result;
        }

        public <T extends Op1> Entry<Op, Boolean> handleProjectExtend(T op, Function<Op, Op> ctor) { //Function<T, Entry<Op, Boolean>> defaultHandler) {
            ProjectExtend pe = ProjectExtend.collect(op);
            Entry<Op, Boolean> result = null;
            Op subOp = op.getSubOp();
            if (pe != null) {
                Op finalSubOp = pe.getSubOp();

//                ExprList filterExprs = null;
//                if (finalSubOp instanceof OpFilter) {
//                    OpFilter f = (OpFilter)finalSubOp;
//                    filterExprs = f.getExprs();
//                    finalSubOp = f.getSubOp();
//                }

                if (finalSubOp instanceof OpGroup)
                {
//                	Op tmp = filterExprs != null ? Op

                    result = wrapWithCache(pe.toOp());
                } else {
                    Entry<Op, Boolean> tmp = rewriteOp(subOp);
                    if (tmp.getValue()) {
                        result = Map.entry(pe.apply(tmp.getKey()), true);
                    }
                }
            }

            if (result == null) {
                // result = rewriteOp(op);
                result = handleOp1(op, ctor);
                // result = defaultHandler.apply(op); // OpRewriter.super.rewrite(op);
            }

            return result;
        }

        @Override
        public Entry<Op, Boolean> fallback(Op op) {
            return Map.entry(op, false);
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpFilter op) {
            Entry<Op, Boolean> result = handleOp1(op, subOp -> OpFilter.filterBy(op.getExprs(), subOp));
            return result;
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpProject op) {
            return handleProjectExtend(op, subOp -> new OpProject(subOp, op.getVars()));
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpExtend op) {
            // return handleProjectExtend(op, OpRewriter.super::rewrite);
            return handleProjectExtend(op, subOp -> OpExtend.create(subOp, op.getVarExprList()));
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpSlice op) {
            Entry<Op, Boolean> result = handleOp1(op, subOp -> new OpSlice(subOp, op.getStart(), op.getLength()));
            return result;
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpDistinct op) {
            Entry<Op, Boolean> result = handleOp1(op, subOp -> new OpDistinct(subOp));
            return result;
        }

        /**
         * The complexity with order by is that we need to be careful about allocated variables
         * (those that start with a dot such as ?.foo).
         * In this method we: (1) inject project nodes and (2) substitute allocated variables in sort conditions
         *
         * <pre>
         * (order ((asc ?.0))
         *   (CACHE
         *     (extend ((?sortKey_1 ?.0))
         *       (group (?s) ((?.0 (min (str ?o)))))
         *     )
         *   )
         * )
         * </pre>
         *
         * <pre>
         * (order ((asc ?sortKey_1))
         *   (CACHE
         *     (project (?sortKey_1))
         *       (extend ((?sortKey_1 ?.0))
         *         (group (?s) ((?.0 (min (str ?o)))))
         *       )
         *     )
         *   )
         * )
         * </pre>
         */
        @Override
        public Entry<Op, Boolean> rewrite(OpOrder op) {
            // Copy op.getConditions()?
            Entry<Op, Boolean> result = handleOp1(op, subOp -> {
                // Check for the pattern: service(<cache:>, service(<REMOTE:>, extend(?.x)))
                Op unwrapped = unwrapIfCached(subOp);
                List<SortCondition> rawConditions = op.getConditions();

                // Defaults
                Op newSubOp = subOp;
                List<SortCondition> conditions = rawConditions;

                if (unwrapped instanceof OpExtend) {
                    OpExtend opExtend = (OpExtend)unwrapped;
                    Map<Var, Expr> varToExpr = opExtend.getVarExprList().getExprs();
                    Map<Var, Var> allocatedVarToActualVar = new HashMap<>();
                    for (Entry<Var, Expr> e : varToExpr.entrySet()) {
                        Var v = e.getKey();
                        Expr expr = e.getValue();
                        if (expr.isVariable()) {
                            Var w = expr.asVar();
                            if (Var.isAllocVar(w)) {
                                allocatedVarToActualVar.computeIfAbsent(w, _w -> v);
                            }
                        }
                    }

                    if (!allocatedVarToActualVar.isEmpty()) {
//                        VarExprList newVel = new VarExprList();
//                        map.forEach((v, e) -> {
//                            if (!(e.isVariable() && inverted.containsKey(e.asVar()))) {
//                                newVel.add(v, e);
//                            }
//                        });

                        NodeTransform xform = NodeTransformLib2.wrapWithNullAsIdentity(allocatedVarToActualVar::get);
    //                    NodeTransform xform = n -> {
    //                        Expr e = map.get(n);
    //                        Node s = e != null && e.isVariable() ? e.asVar() : n;
    //                        return s;
    //                    };

                        //try {
                            // Substitute all ?.x variables with the definition
                            conditions = rawConditions.stream()
                                    .map(sc -> NodeTransformLib2.transform(xform, sc))
                                    .collect(Collectors.toList());
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                            throw ex;
//                        }

                        Set<Var> visibleVars = OpVars.visibleVars(opExtend);
                        List<Var> exposedVars = visibleVars.stream().filter(v -> !Var.isAllocVar(v)).collect(Collectors.toList());
                        newSubOp =
                                wrapWithCacheOp(
                                new OpProject(
                                OpExtend.create(opExtend.getSubOp(), opExtend.getVarExprList()), exposedVars));
                    }

                    // System.err.println("Conditions: " + conditions);
                }

                Op r = new OpOrder(newSubOp, conditions);
                return r;
            });
            return result;
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpUnion op) {
            Entry<Op, Boolean> a = rewriteOp(op.getLeft());
            Entry<Op, Boolean> b = rewriteOp(op.getRight());

            Entry<Op, Boolean> result = a.getValue() && b.getValue()
                    ? Map.entry(new OpUnion(a.getKey(), b.getKey()), true)
                    : Map.entry(op, false);
            return result;
        }

        /**
         * Issue: group may introduce internal variables which are referenced later, such as
         * <pre>
         * (order ((asc ?.0))
         *   (extend ((?sortKey_1 ?.0))
         *     (group (?s) ((?.0 (min (str ?o))))
         * </pre>
         * We need to isolate the group node such that it can by correctly converted back to syntax.
         */
        @Override
        public Entry<Op, Boolean> rewrite(OpGroup op) {
            Entry<Op, Boolean> result = Map.entry(new OpService(CACHE_NODE, new OpService(REMOTE_NODE, op, false), false), true);
            return result;
        }
    }


    /**
     * FIXME This class is not 'broken' but it serves the purpose where the underlying service supports
     * caching with the service enhancer plugin.
     *
     * Broken query rewrite based on the syntax level. The problem is that we need to take care of
     * which parts are executed remotely and which ones are executed in the client.
     * This cannot be cleanly done without the semantics of the algebra: if we have an ElementGroup
     * of which some elements can be cached and other not, then how can we ensure apply the correct transformation
     * if not converting to the algebra first?
     */
    public static class TransformInjectCacheSyntax {

        public static boolean canWrapWithCache(Query query) {
            boolean result = query.hasGroupBy() || query.hasAggregators();
            return result;
        }

        public static Query rewriteQuery(Query query) {
            Query result = query;

            boolean doWrapAsSubQuery = false;
            Element queryPattern = null;
            if (query.isSelectType()) {
                doWrapAsSubQuery = canWrapWithCache(query);
                if (doWrapAsSubQuery) {
                    queryPattern = new ElementSubQuery(query);
                } else {
                    queryPattern = query.getQueryPattern();
                }
            }

            if (queryPattern != null) {
                Element newElt = rewriteInjectCache(queryPattern);
                if (queryPattern != newElt) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Original query:\n" + query);
                    }
                    if (doWrapAsSubQuery) {
                        if (newElt instanceof ElementSubQuery) {
                            result = ((ElementSubQuery)newElt).getQuery();
                        } else {
                            result = new Query();
                            result.setQuerySelectType();
                            result.setQueryResultStar(true);
                            result.setQueryPattern(newElt);
                        }
                    } else {
                        result = QueryTransformOps.shallowCopy(query);
                        result.setQueryPattern(newElt);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rewritten query:\n" + result);
                    }
                }
            }
            return result;
        }

        public static Element rewriteInjectCacheSubQuery(ElementSubQuery elt) {
            Element result;
            Query query = elt.getQuery();

            if (canWrapWithCache(query)) {
    //    		Query newQuery = new Query();
    //    		newQuery.setQuerySelectType();
    //    		newQuery.setQueryResultStar(true);
                // result = new ElementService("cache:env://REMOTE", elt);
                result = new ElementService("cache:", elt);
            } else {
                Query oldQuery = elt.getQuery();
                Query newQuery = rewriteQuery(elt.getQuery());
                result = oldQuery == newQuery
                    ? elt
                    : new ElementSubQuery(newQuery);
            }

            return result;
        }

        public static Element rewriteInjectCache(Element elt) {
            Element result;
            if (elt instanceof ElementGroup) {
                result = rewriteInjectCacheGroup((ElementGroup)elt);
            } else if (elt instanceof ElementSubQuery) {
                result = rewriteInjectCacheSubQuery((ElementSubQuery)elt);
            } else {
                result = elt;
            }
            return result;
        }

        public static Element rewriteInjectCacheGroup(ElementGroup elts) {
            ElementGroup result = new ElementGroup();
            boolean change = false;
            for (Element elt : elts.getElements()) {
                Element newElt = rewriteInjectCache(elt);
                if (newElt != elt) {
                    change = true;
                }
                result.addElement(newElt);
            }

            if (!change) {
                result = elts;
            }
            return result;
        }
    }
}



