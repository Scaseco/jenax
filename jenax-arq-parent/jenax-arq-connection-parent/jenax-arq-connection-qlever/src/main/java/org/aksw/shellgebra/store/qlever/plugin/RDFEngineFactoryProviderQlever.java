package org.aksw.shellgebra.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryProvider;

import jenax.engine.qlever.docker.RDFEngineBuilderQlever;

public class RDFEngineFactoryProviderQlever
    extends ProviderDockerBase<RDFEngineFactory>
    implements RDFEngineFactoryProvider
{
    public static final String PREFIX = QleverConstants.PREFIX;

    public RDFEngineFactoryProviderQlever() {
        this(PREFIX);
    }

    public RDFEngineFactoryProviderQlever(String prefix) {
        super(prefix);
    }

    @Override
    protected RDFEngineFactory provide(String imageName, String tag) {
        return () -> new RDFEngineBuilderQlever<>(imageName, tag);
    }
}
