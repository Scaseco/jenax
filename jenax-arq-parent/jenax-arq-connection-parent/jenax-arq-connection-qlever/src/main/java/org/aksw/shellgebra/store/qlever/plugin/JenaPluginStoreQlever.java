package org.aksw.shellgebra.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactoryRegistry;
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
        RdfDataEngineFactoryRegistry registry = RdfDataEngineFactoryRegistry.get();
        init(registry);
    }

    public static void init(RdfDataEngineFactoryRegistry registry) {
        registry.putEngineProvider(QleverConstants.PREFIX, new RDFEngineFactoryProviderQlever());
        registry.putDatabaseProvider(QleverConstants.PREFIX, new RdfDatabaseFactoryProviderQlever());
    }
}
