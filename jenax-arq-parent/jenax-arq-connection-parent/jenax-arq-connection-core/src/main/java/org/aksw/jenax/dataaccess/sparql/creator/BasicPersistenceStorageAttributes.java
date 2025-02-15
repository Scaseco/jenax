package org.aksw.jenax.dataaccess.sparql.creator;

import java.nio.file.Path;

public interface BasicPersistenceStorageAttributes
    extends RdfEngineCapability
{
    Path getPath();
}
