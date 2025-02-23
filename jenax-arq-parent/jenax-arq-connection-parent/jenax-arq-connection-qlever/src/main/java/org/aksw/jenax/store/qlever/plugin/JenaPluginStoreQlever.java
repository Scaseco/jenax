package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginStoreQlever
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        RDFEngineFactoryRegistry registry = RDFEngineFactoryRegistry.get();
        init(registry);
    }

    public static void init(RDFEngineFactoryRegistry registry) {
        registry.putEngineProvider(QleverConstants.PREFIX, new RDFEngineFactoryProviderQlever());
        registry.putDatabaseProvider(QleverConstants.PREFIX, new RdfDatabaseFactoryProviderQlever());
    }
}
