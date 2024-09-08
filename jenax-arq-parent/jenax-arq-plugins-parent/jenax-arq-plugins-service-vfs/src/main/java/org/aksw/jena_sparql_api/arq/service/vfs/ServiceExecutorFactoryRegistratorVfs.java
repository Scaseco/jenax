package org.aksw.jena_sparql_api.arq.service.vfs;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.util.Context;

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
    };

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
            QueryIterator result = CONCURRENT.equals(serviceNode)
                ? new QueryIterConcurrentSimple(input, execCxt, executorService, maxConcurrentTasks) {
                    @Override
                    protected QueryIterator nextStage(Binding binding) {
                        QueryIterator singleton = QueryIterSingleton.create(binding, execCxt);
                        return chain.createExecution(opService, singleton, execCxt);
                    }
                }
                : chain.createExecution(opService, input, execCxt);
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

        reg.addBulkLink(new ChainingServiceExecutorConcurrentSimple(ForkJoinPool.commonPool(), Runtime.getRuntime().availableProcessors()));
        // ServiceExecutorRegistry.set(cxt, FACTORY);
    }
}
