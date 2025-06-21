package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFDatabaseFactoryProvider;
import org.aksw.jenax.engine.qlever.RDFDatabaseBuilderQlever;

public class RDFDatabaseFactoryProviderQlever
    extends ProviderDockerBase<RDFDatabaseFactory>
    implements RDFDatabaseFactoryProvider
{
    public static final String PREFIX = QleverConstants.PREFIX;

    public RDFDatabaseFactoryProviderQlever() {
        this(PREFIX);
    }

    public RDFDatabaseFactoryProviderQlever(String prefix) {
        super(prefix);
    }

    @Override
    protected RDFDatabaseFactory provide(String imageName, String tag) {
        return () -> {
            RDFDatabaseBuilderQlever<?> r = new RDFDatabaseBuilderQlever<>();
            r.setDockerImageName(imageName);
            r.setDockerImageTag(tag);
            return r;
        };
    }
}
