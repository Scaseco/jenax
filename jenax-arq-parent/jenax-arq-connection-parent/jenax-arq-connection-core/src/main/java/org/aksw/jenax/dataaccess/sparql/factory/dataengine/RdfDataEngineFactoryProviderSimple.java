package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

/** A provider that maps a specific name to a specific factory. */
public class RdfDataEngineFactoryProviderSimple
    implements RdfDataEngineFactoryProvider
{
    protected String factoryName;
    protected RdfDataEngineFactory factory;

    public RdfDataEngineFactoryProviderSimple(String name, RdfDataEngineFactory factory) {
        super();
        this.factoryName = Objects.requireNonNull(name);
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public RdfDataEngineFactory create(String name) {
        RdfDataEngineFactory result = (this.factoryName.equals(name))
            ? factory
            : null;
        return result;
    }
}
