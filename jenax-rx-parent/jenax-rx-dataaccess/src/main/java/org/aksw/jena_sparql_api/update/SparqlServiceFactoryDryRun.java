package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jenax.connectionless.SparqlService;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.execution.factory.update.UpdateExecutionFactory;
import org.apache.jena.sparql.core.DatasetDescription;

import java.net.http.HttpClient;

/**
 * A SPARQL service factory that intercepts update request and keeps track
 * of added/removed quads in a different backend.
 *
 *
 *
 * @author raven
 *
 */
public class SparqlServiceFactoryDryRun
    implements SparqlServiceFactory
{
    protected SparqlServiceFactory delegate;

    public SparqlServiceFactoryDryRun(SparqlServiceFactory delegate) {
        super();
        this.delegate = delegate;
    }


    @Override
    public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {

        SparqlService sparqlService = delegate.createSparqlService(serviceUri, datasetDescription, httpClient);

        QueryExecutionFactory wrappedQef = null;
        UpdateExecutionFactory wrappedUef = null;

        SparqlService result = new SparqlServiceImpl(wrappedQef, wrappedUef);
        if(true) {
            throw new RuntimeException("not implemented yet");
        }

        return result;
    }

}
