package org.aksw.shellgebra.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDatabaseFactoryProvider;
import org.aksw.jenax.engine.qlever.RdfDatabaseBuilderQlever;

public class RdfDatabaseFactoryProviderQlever
    extends ProviderDockerBase<RdfDatabaseFactory>
    implements RdfDatabaseFactoryProvider
{
    public static final String PREFIX = QleverConstants.PREFIX;

    public RdfDatabaseFactoryProviderQlever() {
        this(PREFIX);
    }

    public RdfDatabaseFactoryProviderQlever(String prefix) {
        super(prefix);
    }

    @Override
    protected RdfDatabaseFactory provide(String imageName, String tag) {
        return () -> new RdfDatabaseBuilderQlever();
    }
}
