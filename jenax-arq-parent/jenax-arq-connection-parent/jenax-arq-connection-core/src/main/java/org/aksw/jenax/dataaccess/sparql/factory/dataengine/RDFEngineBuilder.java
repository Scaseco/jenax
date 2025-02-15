package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Optional;

import org.aksw.jenax.dataaccess.sparql.creator.RdfEngineCapability;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicMutable;

public interface RDFEngineBuilder<X extends RDFEngineBuilder<X>>
    extends RdfDataSourceSpecBasicMutable<X>
{
    /**
     * Return information about the engine being built based on the current snapshot
     * of the configuration.
     * For drivers can use this to expose the currently effective path from which the engine
     * will be initialized and/or started.
     */
    default <T extends RdfEngineCapability> Optional<T> getAttributeSnapshot(Class<T> clazz) {
        return Optional.empty();
    }

    RdfDataEngine build() throws Exception;
}
