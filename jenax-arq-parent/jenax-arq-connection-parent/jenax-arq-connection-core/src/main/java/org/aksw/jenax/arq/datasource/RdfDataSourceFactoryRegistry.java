package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A global registry for instances of
 * {@link RdfDataSourceFactory} and {@link RdfDataSourceDecorator}.
 *
 * This class provides the infrastructure for third party plugins.
 *
 *
 * @author raven
 *
 */
public class RdfDataSourceFactoryRegistry {
    private static RdfDataSourceFactoryRegistry INSTANCE;

    public static RdfDataSourceFactoryRegistry get() {
        if (INSTANCE == null) {
            synchronized (RdfDataSourceFactoryRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RdfDataSourceFactoryRegistry();
                }
            }
        }
        return INSTANCE;
    }

    protected Map<String, RdfDataSourceFactory> factoryRegistry = new ConcurrentHashMap<>();
    protected Map<String, RdfDataSourceDecorator> decoratorRegistry = new ConcurrentHashMap<>();


    public RdfDataSourceFactoryRegistry putFactory(String name, RdfDataSourceFactory factory) {
        factoryRegistry.put(name, factory);
        return this;
    }

    public RdfDataSourceFactory getFactory(String name) {
        return factoryRegistry.get(name);
    }


    public RdfDataSourceFactoryRegistry putDecorator(String name, RdfDataSourceDecorator factory) {
        decoratorRegistry.put(name, factory);
        return this;
    }

    public RdfDataSourceDecorator getDecorator(String name) {
        return decoratorRegistry.get(name);
    }

}
