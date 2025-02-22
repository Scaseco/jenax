package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginExecTracker
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
        reg.add(new QueryEngineFactoryExecTracker());
    }

    public static void init(UpdateEngineRegistry reg) {
        reg.add(new UpdateEngineFactoryExecTracker());
    }

    @Override
    public int level() {
        return 1000000;
    }
}
