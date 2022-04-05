package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A global registry for instances of
 * {@link RdfDataEngineFactory} and {@link RdfDataSourceDecorator}.
 *
 * This class provides the infrastructure for third party plugins.
 *
 *
 * @author raven
 *
 */
public class RdfDataEngineFactoryRegistry {
    private static RdfDataEngineFactoryRegistry INSTANCE;

    public static RdfDataEngineFactoryRegistry get() {
        if (INSTANCE == null) {
            synchronized (RdfDataEngineFactoryRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RdfDataEngineFactoryRegistry();
                }
            }
        }
        return INSTANCE;
    }

    protected Map<String, RdfDataEngineFactory> factoryRegistry = new ConcurrentHashMap<>();
    protected Map<String, RdfDataSourceDecorator> decoratorRegistry = new ConcurrentHashMap<>();


    public RdfDataEngineFactoryRegistry putFactory(String name, RdfDataEngineFactory factory) {
        factoryRegistry.put(name, factory);
        return this;
    }

    public RdfDataEngineFactory getFactory(String name) {
        return factoryRegistry.get(name);
    }


    public RdfDataEngineFactoryRegistry putDecorator(String name, RdfDataSourceDecorator factory) {
        decoratorRegistry.put(name, factory);
        return this;
    }

    public RdfDataSourceDecorator getDecorator(String name) {
        return decoratorRegistry.get(name);
    }

}
