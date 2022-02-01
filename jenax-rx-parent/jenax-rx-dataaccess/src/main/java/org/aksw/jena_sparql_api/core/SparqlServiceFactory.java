package org.aksw.jena_sparql_api.core;

import org.aksw.jenax.connectionless.SparqlService;
import org.apache.jena.sparql.core.DatasetDescription;

import java.net.http.HttpClient;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 *
 * @author raven
 *
 */
public interface SparqlServiceFactory {
    SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient);
}
