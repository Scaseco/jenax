package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;

/**
 * A registry for instances of
 * {@link RdfDataEngineFactory} and {@link RdfDataSourceDecorator}.
 *
 * This class provides the infrastructure for third party plugins.
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

    protected Map<String, RdfDataEngineFactoryProvider> engineProviderRegistry = Collections.synchronizedMap(new LinkedHashMap<>());
    protected Map<String, RdfDatabaseFactoryProvider> databaseProviderRegistry = Collections.synchronizedMap(new LinkedHashMap<>());

    protected Map<String, RdfDataSourceDecorator> decoratorRegistry = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Create and register a provider with the given argument name that returns the given factory when
     * requesting that name.
     * This method will override a previously registered provider with the same name.
     */
    public RdfDataEngineFactoryRegistry putFactory(String providerAndFactoryName, RdfDataEngineFactory factory) {
        RdfDataEngineFactoryProviderSimple provider = new RdfDataEngineFactoryProviderSimple(providerAndFactoryName, factory);
        putEngineProvider(providerAndFactoryName, provider);
        return this;
    }

    /** Registers a provider under a specific name. The name only identifies the provider. */
    public RdfDataEngineFactoryRegistry putEngineProvider(String providerName, RdfDataEngineFactoryProvider provider) {
        engineProviderRegistry.put(providerName, provider);
        return this;
    }

    public RdfDataEngineFactoryRegistry putDatabaseProvider(String providerName, RdfDatabaseFactoryProvider provider) {
        databaseProviderRegistry.put(providerName, provider);
        return this;
    }

    /** Get a provider by its name.
     * @return */
    public RdfDataEngineFactoryProvider getProvider(String name) {
        return engineProviderRegistry.get(name);
    }

    private static <T> T provide(Map<String, ? extends Provider<? extends T>> providerMap, String name) {
        T result = providerMap.entrySet().stream()
            .map(e -> {
                Provider<? extends T> provider = e.getValue();
                T r = provider.create(name);
                return r;
            })
            .findFirst()
            .orElse(null);
        return result;
    }

    public RdfDataStore getStore(String name) {
        RdfDataEngineFactory engineFactory = getEngineFactory(name);
        RdfDatabaseFactory databaseFactory = getDatabaseFactory(name);
        return new RdfDataStore(engineFactory, databaseFactory);
    }

    @Deprecated // Use getEngineFactory
    public RdfDataEngineFactory getFactory(String name) {
        return getEngineFactory(name);
    }
    public RdfDataEngineFactory getEngineFactory(String name) {
        RdfDataEngineFactory result = provide(engineProviderRegistry, name);
        return result;
    }

    public RdfDatabaseFactory getDatabaseFactory(String name) {
        RdfDatabaseFactory result = provide(databaseProviderRegistry, name);
        return result;
    }

    public RdfDataEngineFactoryRegistry putDecorator(String name, RdfDataSourceDecorator factory) {
        decoratorRegistry.put(name, factory);
        return this;
    }

    public RdfDataSourceDecorator getDecorator(String name) {
        return decoratorRegistry.get(name);
    }
}
