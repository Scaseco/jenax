package org.aksw.jenax.arq.service.vfs;

import org.aksw.jenax.arq.service.vfs.ServiceExecutorFactoryVfsUtils.PathSpec;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
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
            QueryIterator result;
            Node serviceNode = opExecute.getService();
            Context cxt = execCxt.getContext();

            try {
                PathSpec pathSpec = ServiceExecutorFactoryVfsUtils.toPathSpec(serviceNode, cxt);
                if (pathSpec != null) {
                    result = ServiceExecutorFactoryVfsUtils.nextStage(opExecute, binding, execCxt, pathSpec);
                } else {
                    result = chain.createExecution(opExecute, original, binding, execCxt);
                }
            } catch (Exception e) {
                throw new QueryExecException(e);
            }

            return result;
        }
    }

    public static void register(Context cxt) {
        ServiceExecutorRegistry reg = cxt.computeIfAbsent(ARQConstants.registryServiceExecutors, s -> new ServiceExecutorRegistry());
        reg.addSingleLink(new ChainingServiceExecutorVfs());
    }
}
