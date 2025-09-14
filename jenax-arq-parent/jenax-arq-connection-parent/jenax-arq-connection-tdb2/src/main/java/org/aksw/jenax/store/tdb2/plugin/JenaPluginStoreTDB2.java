package org.aksw.jenax.store.tdb2.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.aksw.jenax.store.tdb2.RDFDatabaseFactoryProviderTDB2;
import org.aksw.jenax.store.tdb2.RDFEngineFactoryProviderTDB2;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginStoreTDB2
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        // RDFEngineFactoryRegistry system registrations.
        RDFEngineFactoryRegistry registry = RDFEngineFactoryRegistry.get();
        init(registry);
    }

    public static void init(RDFEngineFactoryRegistry registry) {
        registry.putDatabaseProvider(TDB2Constants.PREFIX, new RDFDatabaseFactoryProviderTDB2());
        registry.putEngineProvider(TDB2Constants.PREFIX, new RDFEngineFactoryProviderTDB2());
    }
}
