package org.aksw.jenax.dataaccess.sparql.dataset.engine;

import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

/**
 * Plugin that registers a query and update engine for {@link DatasetGraphOverRDFEngine}.
 */
public class JenaPluginDatasetGraphOverRDFEngine
    implements JenaSubsystemLifecycle
{
    @Override
    public void start() {
        QueryEngineRegistry queryReg = QueryEngineRegistry.get();
        init(queryReg);

        UpdateEngineRegistry updateReg = UpdateEngineRegistry.get();
        init(updateReg);
    }

    @Override
    public void stop() {
    }

    public static void init(QueryEngineRegistry reg) {
        reg.add(new QueryEngineFactoryOverRDFDataSource());
    }

    public static void init(UpdateEngineRegistry reg) {
         // reg.add(new UpdateEngineFactoryover());
    }

    @Override
    public int level() {
        return 1000000;
    }
}
