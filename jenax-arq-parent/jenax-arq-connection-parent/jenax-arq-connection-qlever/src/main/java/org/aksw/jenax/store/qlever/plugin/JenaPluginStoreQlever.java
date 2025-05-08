package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.aksw.jenax.store.qlever.assembler.DatasetAssemblerQlever;
import org.aksw.jenax.store.qlever.assembler.QleverAssemblerVocab;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
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
        // Assembler registration.
        registerWith(Assembler.general());

        // RDFEngineFactoryRegistry system registrations.
        RDFEngineFactoryRegistry registry = RDFEngineFactoryRegistry.get();
        init(registry);
    }

    public static void init(RDFEngineFactoryRegistry registry) {
        registry.putEngineProvider(QleverConstants.PREFIX, new RDFEngineFactoryProviderQlever());
        registry.putDatabaseProvider(QleverConstants.PREFIX, new RdfDatabaseFactoryProviderQlever());
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, QleverAssemblerVocab.Dataset, new DatasetAssemblerQlever(), DatasetAssembler.getGeneralType());

        // Note: We can't install the plugin on graphs because they don't have a context
    }

}
