package org.aksw.jenax.store.tdb2;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryProvider;
import org.aksw.jenax.store.tdb2.plugin.TDB2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFEngineFactoryProviderTDB2
    implements RDFEngineFactoryProvider
{
    private static final Logger logger = LoggerFactory.getLogger(RDFEngineFactoryProviderTDB2.class);

    public static final String PREFIX = TDB2Constants.PREFIX;

    private final String prefix;

    public RDFEngineFactoryProviderTDB2() {
        this(PREFIX);
    }

    public RDFEngineFactoryProviderTDB2(String prefix) {
        super();
        this.prefix = prefix;
    }

    @Override
    public RDFEngineFactory create(String name) {
        RDFEngineFactory result = null;
        if (name.startsWith(prefix)) {
            String suffix = name.substring(prefix.length());
            if (!suffix.isEmpty()) {
                logger.warn("Unexpected suffix on name " + name + ": " + suffix);
            }

            result = () -> new RDFEngineBuilderTDB2<>();
        }
        return result;
    }
}
