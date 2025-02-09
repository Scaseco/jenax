package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

/**
 * A factory provider can create {@link RdfDataEngineFactory} instances for
 * all the names it supports.
 * For example, some providers may support specific "dockerImageName:tag" names.
 * Providers can be registered at a {@link RdfDataEngineFactoryRegistry}.
 */
public interface RdfDataEngineFactoryProvider
    extends Provider<RdfDataEngineFactory>
{
}
