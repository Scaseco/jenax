package org.aksw.jenax.arq.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.algebra.transform.ProjectExtend;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.datasource.RdfDataSourceDelegateBase;
import org.aksw.jenax.sparql.algebra.topdown.OpRewriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rewrite that attempts to inject cache operations around group by computations.
 *
 * SERVICE <cache:> { SERVICE <env:REMOTE> { GROUP-BY } }
 */
public class RdfDataSourceWithLocalCache
    extends RdfDataSourceDelegateBase
{
    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithLocalCache.class);

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
        Query query = QueryFactory.create(queryStr);

        // RdfDataSourceWithLocalCache.createProxyDataset(RdfDataEngines.of(DatasetFactory.create()))
        RdfDataSourceWithLocalCache dataSource = new RdfDataSourceWithLocalCache(RdfDataEngines.of(DatasetFactory.create()));
        Query rewritten = OpRewriteInjectCacheOps.rewriteQuery(query);
        System.out.println(rewritten);
    }



    public RdfDataSourceWithLocalCache(RdfDataSource delegate) {
        super(delegate);
        proxyDataset = createProxyDataset(delegate);
    }

    public static Dataset createProxyDataset(RdfDataSource delegate) {
        Dataset result = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().equals(REMOTE_NODE)) {
                RDFConnection base = delegate.getConnection();
                r = RDFConnectionUtils.execService(opExec, base);
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

    /** Rewriter that injects caching operations after group by operations */
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

        public Entry<Op, Boolean> handleOp1(Op op, Function<Op, Op> ctor) {
            Entry<Op, Boolean> tmp = rewriteOp(op);
            Op subOp = tmp.getKey();
            boolean rewritten = tmp.getValue();
            Entry<Op, Boolean> result = rewritten
                    ? Map.entry(ctor.apply(subOp), true)
                    : Map.entry(op, false);
            return result;
        }

        public <T extends Op1> Entry<Op, Boolean> handleProjectExtend(T op, Function<Op, Op> ctor) { //Function<T, Entry<Op, Boolean>> defaultHandler) {
            ProjectExtend pe = ProjectExtend.collect(op);
            Entry<Op, Boolean> result = null;
            Op subOp = op.getSubOp();
            if (pe != null) {
                if (pe.getSubOp() instanceof OpGroup) {
                    result = wrapWithCache(pe.toOp());
                } else {
                    Entry<Op, Boolean> tmp = rewriteOp(subOp);
                    if (tmp.getValue()) {
                        result = Map.entry(pe.apply(tmp.getKey()), true);
                    }
                }
            }

            if (result == null) {
                result = handleOp1(subOp, ctor);
                // result = defaultHandler.apply(op); // OpRewriter.super.rewrite(op);
            }

            return result;
        }

        @Override
        public Entry<Op, Boolean> fallback(Op op) {
            return Map.entry(op, false);
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
            Entry<Op, Boolean> result = handleOp1(op.getSubOp(), subOp -> new OpSlice(subOp, op.getStart(), op.getLength()));
            return result;
        }

        @Override
        public Entry<Op, Boolean> rewrite(OpDistinct op) {
            Entry<Op, Boolean> result = handleOp1(op.getSubOp(), subOp -> new OpDistinct(subOp));
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
            Entry<Op, Boolean> result = handleOp1(op.getSubOp(), subOp -> {
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
}
