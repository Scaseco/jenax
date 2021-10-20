package org.aksw.jena_sparql_api.arq.service.vfs;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.ServiceExecution;
import org.apache.jena.sparql.service.ServiceExecutorFactory;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.util.Context;

public class ServiceExecutorFactoryRegistratorVfs {
    public static final ServiceExecutorFactory FACTORY = new ServiceExecutorFactory() {
        @Override
        public ServiceExecution createExecutor(OpService opExecute, OpService original, Binding binding,
                ExecutionContext execCxt) {
            Node serviceNode = opExecute.getService();
            Entry<Path, Map<String, String>> fileSpec = ServiceExecutorFactoryVfsUtils.toPathSpec(serviceNode);

            ServiceExecution result = fileSpec == null
                    ? null
                    : () -> ServiceExecutorFactoryVfsUtils.nextStage(opExecute, binding, execCxt, fileSpec.getKey(), fileSpec.getValue());

            return result;
        }
    };

    public static void register(Context cxt) {

        ServiceExecutorRegistry reg = ServiceExecutorRegistry.get(cxt);
        if (reg == null) {
            reg = new ServiceExecutorRegistry();
            ServiceExecutorRegistry.set(cxt, reg);
        }

        reg.add(FACTORY);
        // ServiceExecutorRegistry.set(cxt, FACTORY);
    }
}
