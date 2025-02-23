package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseFactory;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceDecorator;
import org.apache.jena.sys.JenaSystem;

/**
 * A registry for instances of
 * {@link RDFEngineFactory} and {@link RdfDataSourceDecorator}.
 *
 * This class provides the infrastructure for third party plugins.
 */
public class RDFEngineFactoryRegistry {

    static { JenaSystem.init(); }

    private static RDFEngineFactoryRegistry INSTANCE;

    public static RDFEngineFactoryRegistry get() {
        if (INSTANCE == null) {
            synchronized (RDFEngineFactoryRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RDFEngineFactoryRegistry();
                }
            }
        }
        return INSTANCE;
    }

    protected Map<String, RDFEngineFactoryProvider> engineProviderRegistry = Collections.synchronizedMap(new LinkedHashMap<>());
    protected Map<String, RdfDatabaseFactoryProvider> databaseProviderRegistry = Collections.synchronizedMap(new LinkedHashMap<>());

    protected Map<String, RdfDataSourceDecorator> decoratorRegistry = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Create and register a provider with the given argument name that returns the given factory when
     * requesting that name.
     * This method will override a previously registered provider with the same name.
     */
    public RDFEngineFactoryRegistry putFactory(String providerAndFactoryName, RDFEngineFactory factory) {
        Objects.requireNonNull(providerAndFactoryName);
        Objects.requireNonNull(factory);
        RdfDataEngineFactoryProviderSimple provider = new RdfDataEngineFactoryProviderSimple(providerAndFactoryName, factory);
        putEngineProvider(providerAndFactoryName, provider);
        return this;
    }

    /** Registers a provider under a specific name. The name only identifies the provider. */
    public RDFEngineFactoryRegistry putEngineProvider(String providerName, RDFEngineFactoryProvider provider) {
        Objects.requireNonNull(providerName);
        Objects.requireNonNull(provider);
        engineProviderRegistry.put(providerName, provider);
        return this;
    }

    public RDFEngineFactoryRegistry putDatabaseProvider(String providerName, RdfDatabaseFactoryProvider provider) {
        Objects.requireNonNull(providerName);
        Objects.requireNonNull(provider);
        databaseProviderRegistry.put(providerName, provider);
        return this;
    }

    /** Get a provider by its name.
     * @return */
    public RDFEngineFactoryProvider getProvider(String name) {
        return engineProviderRegistry.get(name);
    }

    private static <T> T provide(Map<String, ? extends Provider<? extends T>> providerMap, String name) {
        T result = providerMap.entrySet().stream()
            .flatMap(e -> {
                Provider<? extends T> provider = e.getValue();
                T r = provider.create(name);
                return Stream.ofNullable(r);
            })
            .findFirst()
            .orElse(null);
        return result;
    }

    public RdfDataStore getStore(String name) {
        RDFEngineFactory engineFactory = getEngineFactory(name);
        RDFDatabaseFactory databaseFactory = getDatabaseFactory(name);
        return new RdfDataStore(engineFactory, databaseFactory);
    }

    @Deprecated // Use getEngineFactory
    public RDFEngineFactory getFactory(String name) {
        return getEngineFactory(name);
    }
    public RDFEngineFactory getEngineFactory(String name) {
        RDFEngineFactory result = provide(engineProviderRegistry, name);
        return result;
    }

    public RDFDatabaseFactory getDatabaseFactory(String name) {
        RDFDatabaseFactory result = provide(databaseProviderRegistry, name);
        return result;
    }

    public RDFEngineFactoryRegistry putDecorator(String name, RdfDataSourceDecorator factory) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(factory);
        decoratorRegistry.put(name, factory);
        return this;
    }

    public RdfDataSourceDecorator getDecorator(String name) {
        return decoratorRegistry.get(name);
    }
}
