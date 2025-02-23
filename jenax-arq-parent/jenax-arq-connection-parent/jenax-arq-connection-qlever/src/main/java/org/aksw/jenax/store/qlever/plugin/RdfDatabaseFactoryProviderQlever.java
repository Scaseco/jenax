package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDatabaseFactoryProvider;
import org.aksw.jenax.engine.qlever.RdfDatabaseBuilderQlever;

public class RdfDatabaseFactoryProviderQlever
    extends ProviderDockerBase<RDFDatabaseFactory>
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
    protected RDFDatabaseFactory provide(String imageName, String tag) {
        return () -> new RdfDatabaseBuilderQlever();
    }
}
