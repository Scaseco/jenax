package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactoryProvider;

import jenax.engine.qlever.docker.RdfDataEngineBuilderQlever;

public class RdfDataEngineFactoryProviderQlever
    extends ProviderDockerBase<RdfDataEngineFactory>
    implements RdfDataEngineFactoryProvider
{
    public static final String PREFIX = QleverConstants.PREFIX;

    public RdfDataEngineFactoryProviderQlever() {
        this(PREFIX);
    }

    public RdfDataEngineFactoryProviderQlever(String prefix) {
        super(prefix);
    }

    @Override
    protected RdfDataEngineFactory provide(String imageName, String tag) {
        return () -> new RdfDataEngineBuilderQlever<>(imageName, tag);
    }
}
