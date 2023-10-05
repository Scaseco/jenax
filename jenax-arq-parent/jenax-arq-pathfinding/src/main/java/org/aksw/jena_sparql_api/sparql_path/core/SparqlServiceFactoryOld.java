package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Collection;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 *
 * @author raven
 *
 */
public interface SparqlServiceFactoryOld {
    QueryExecutionFactory createSparqlService(String serviceUri, Collection<String> defaultGraphUris);
}
