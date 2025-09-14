package org.aksw.jenax.store.tdb2;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFDatabaseFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFDatabaseFactoryProviderTDB2
    implements RDFDatabaseFactoryProvider
{
    private static final Logger logger = LoggerFactory.getLogger(RDFDatabaseFactoryProviderTDB2.class);

    public static final String PREFIX = "tdb2"; // QleverConstants.PREFIX;

    private final String prefix;

    public RDFDatabaseFactoryProviderTDB2() {
        this(PREFIX);
    }

    public RDFDatabaseFactoryProviderTDB2(String prefix) {
        super();
        this.prefix = prefix;
    }

    @Override
    public RDFDatabaseFactory create(String name) {
        RDFDatabaseFactory result = null;
        if (name.startsWith(prefix)) {
            String suffix = name.substring(prefix.length());
            if (!suffix.isEmpty()) {
                logger.warn("Unexpected suffix on name " + name + ": " + suffix);
            }

            result = () -> new RDFDatabaseBuilderTDB2<>();
        }
        return result;
    }
}
