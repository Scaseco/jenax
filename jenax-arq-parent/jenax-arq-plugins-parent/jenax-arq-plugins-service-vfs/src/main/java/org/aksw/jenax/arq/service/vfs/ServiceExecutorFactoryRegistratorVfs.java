package org.aksw.jenax.arq.service.vfs;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.algebra.optimize.RewriteFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.util.Context;

import com.google.common.util.concurrent.MoreExecutors;

public class ServiceExecutorFactoryRegistratorVfs {
    public static class ChainingServiceExecutorVfs
        implements ChainingServiceExecutor
    {
        @Override
        public QueryIterator createExecution(OpService opExecute, OpService original, Binding binding,
                ExecutionContext execCxt, ServiceExecutor chain) {
            Node serviceNode = opExecute.getService();
            Entry<Path, Map<String, String>> fileSpec = ServiceExecutorFactoryVfsUtils.toPathSpec(serviceNode);

            QueryIterator result = fileSpec == null
                    ? chain.createExecution(opExecute, original, binding, execCxt)
                    : ServiceExecutorFactoryVfsUtils.nextStage(opExecute, binding, execCxt, fileSpec.getKey(), fileSpec.getValue());

            return result;
        }
    }

    /** Read ahead a certain amount of items from the lhs and
     *  concurrently start fetching the corresponding data for the rhs.
     *  Incorrect use may waste resources by reading ahead too many
     *  items that will not be needed. */
    public static class ChainingServiceExecutorConcurrentSimple
        implements ChainingServiceExecutorBulk
    {
        protected static final Node CONCURRENT = NodeFactory.createURI("urn:concurrent");

        protected ExecutorService executorService;
        protected int maxConcurrentTasks;

        public ChainingServiceExecutorConcurrentSimple(ExecutorService executorService, int maxConcurrentTasks) {
            super();
            this.executorService = Objects.requireNonNull(executorService);
            this.maxConcurrentTasks = maxConcurrentTasks;
        }

        @Override
        public QueryIterator createExecution(OpService opService, QueryIterator input, ExecutionContext execCxt,
                ServiceExecutorBulk chain) {
            Node serviceNode = opService.getService();
            // XXX Parse out maxConcurrentTasks from the IRI e.g. concurrent+10:
            QueryIterator result;
            if (CONCURRENT.equals(serviceNode)) {
                // execCxt.getExecutor().create(execCxt).executeOp(opService, input);
                Context cxt = execCxt.getContext();
                RewriteFactory rf = QueryExecUtils.decideOptimizer(cxt);
                Rewrite rw = rf.create(cxt);
                Op subOp = opService.getSubOp();
                Op optimizedSubOp = rw.rewrite(subOp);


//                List<Binding> list = IteratorUtils.toList(input);
//                System.out.println("SAW ITEM COUNT: " + list.size());
//                input = QueryIterPlainWrapper.create(list.iterator(), execCxt);

                result = new QueryIterRepeatApplyConcurrent(input, execCxt, executorService, maxConcurrentTasks) {
                    @Override
                    protected QueryIterator nextStage(Binding binding, ExecutionContext localExecCxt) {
                        // Op finalSubOp = QC.substitute(opService.getSubOp(), binding);
                        // QueryIterator singleton = QueryIterSingleton.create(binding, execCxt);
                        return QC.execute(optimizedSubOp, binding, localExecCxt); // chain.createExecution(opService, singleton, execCxt);
                    }
                };
            } else {
                result = chain.createExecution(opService, input, execCxt);
            }
            return result;
        }
    }

    public static void register(Context cxt) {

        ServiceExecutorRegistry reg = ServiceExecutorRegistry.get(cxt);
        if (reg == null) {
            reg = new ServiceExecutorRegistry();
            ServiceExecutorRegistry.set(cxt, reg);
        }

        reg.addSingleLink(new ChainingServiceExecutorVfs());

        ExecutorService executorService = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());

        // Make sure to add the 'concurrent' handler after the service enhancer
        // because we require 'loop:' to be processed first - i.e. the order is SERVICE <loop:concurrent:>
        List<ChainingServiceExecutorBulk> bulkChain = reg.getBulkChain();
        int idx = IntStream.range(0, bulkChain.size())
                .filter(i -> bulkChain.get(i).getClass().equals(ChainingServiceExecutorBulkServiceEnhancer.class))
                .findFirst()
                .orElse(-1);

        int maxTaskCount = Runtime.getRuntime().availableProcessors();
        // maxTaskCount = 1;
        // bulkChain.add(idx + 1, new ChainingServiceExecutorConcurrentSimple(executorService, maxTaskCount));
    }
}
