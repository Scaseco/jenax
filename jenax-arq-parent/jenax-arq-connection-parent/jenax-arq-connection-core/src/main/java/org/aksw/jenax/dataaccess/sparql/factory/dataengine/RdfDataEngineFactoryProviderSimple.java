package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

/** A provider that maps a specific name to a specific factory. */
public class RdfDataEngineFactoryProviderSimple
    implements RDFEngineFactoryProvider
{
    protected String factoryName;
    protected RDFEngineFactory factory;

    public RdfDataEngineFactoryProviderSimple(String name, RDFEngineFactory factory) {
        super();
        this.factoryName = Objects.requireNonNull(name);
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public RDFEngineFactory create(String name) {
        RDFEngineFactory result = (this.factoryName.equals(name))
            ? factory
            : null;
        return result;
    }
}
