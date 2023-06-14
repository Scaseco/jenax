package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.algebra.transform.ProjectExtend;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.datasource.RdfDataSourceDelegateBase;
import org.aksw.jenax.sparql.algebra.topdown.OpRewriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;

public class RdfDataSourceWithLocalCache
    extends RdfDataSourceDelegateBase
{
    public static final String REMOTE_IRI = "env://REMOTE";
    public static final Node REMOTE_NODE = NodeFactory.createURI(REMOTE_IRI);

    public static final Node CACHE_NODE = NodeFactory.createURI("cache:");

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    public Dataset proxyDataset;

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

        MyRewriter rewriter = new MyRewriter();

        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base, query -> {
            Query r = QueryUtils.applyOpTransform(query, op -> {
                // Node serviceNode = NodeFactory.createURI("cache:" + REMOTE_IRI);

                Entry<Op, Boolean> e = rewriter.rewriteOp(op);

                Op rr = e.getValue()
                        ? e.getKey()
                        : wrapWithCache(op).getKey(); // new OpService(serviceNode, op, false);

//                System.err.println("OriginalQuery:\n" + query);
//                System.err.println("Cache rewrite [pushed=" + e.getValue() + "]: " + rr);

                return rr;
            });
            return r;
        });

        return result;
    }

    public static Entry<Op, Boolean> wrapWithCache(Op op) {
        Entry<Op, Boolean> result = Map.entry(new OpService(CACHE_NODE, new OpService(REMOTE_NODE, op, false), false), true);
        return result;
    }

    public static class MyRewriter
        implements OpRewriter<Entry<Op, Boolean>> {

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

        @Override
        public Entry<Op, Boolean> rewrite(OpOrder op) {
            // Copy op.getConditions()?
            Entry<Op, Boolean> result = handleOp1(op.getSubOp(), subOp -> new OpOrder(subOp, op.getConditions()));
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

        @Override
        public Entry<Op, Boolean> rewrite(OpGroup op) {
            Entry<Op, Boolean> result = Map.entry(new OpService(CACHE_NODE, new OpService(REMOTE_NODE, op, false), false), true);
            return result;
        }
    }
}
