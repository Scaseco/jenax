package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Collection;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 * 
 * @author raven
 *
 */
public interface SparqlServiceFactoryOld {
    QueryExecutionFactory createSparqlService(String serviceUri, Collection<String> defaultGraphUris);
}
