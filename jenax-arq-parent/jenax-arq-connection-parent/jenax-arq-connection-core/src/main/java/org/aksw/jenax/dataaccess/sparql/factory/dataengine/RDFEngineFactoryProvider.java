package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

/**
 * A factory provider can create {@link RDFEngineFactory} instances for
 * all the names it supports.
 * For example, some providers may support specific "dockerImageName:tag" names.
 * Providers can be registered at a {@link RdfDataEngineFactoryRegistry}.
 */
public interface RDFEngineFactoryProvider
    extends Provider<RDFEngineFactory>
{
}
