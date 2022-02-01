package org.aksw.jena_sparql_api.core;

import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import java.net.http.HttpClient;

public class UpdateExecutionFactoryHttp
    extends UpdateExecutionFactoryParsingBase
{
    private String remoteEndpoint;
    //private HttpAuthenticator authenticator;
    private HttpClient httpClient;
    private DatasetDescription datasetDescription;

    public UpdateExecutionFactoryHttp(String remoteEndpoint) {
        this(remoteEndpoint, null);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, HttpClient httpClient) {
        this(remoteEndpoint, new DatasetDescription(), httpClient);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, DatasetDescription datasetDescription, HttpClient httpClient) {
        this.remoteEndpoint = remoteEndpoint;
        this.datasetDescription = datasetDescription;
        this.httpClient = httpClient;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        // Fixing var names should be done with transform
        // UpdateRequestUtils.fixVarNames(updateRequest);

        UpdateExecutionHTTPBuilder builder = UpdateExecutionHTTPBuilder.create()
                .update(updateRequest)
                .httpClient(httpClient)
                .endpoint(remoteEndpoint);
        datasetDescription.getNamedGraphURIs().forEach(builder::addUsingNamedGraphURI);
        datasetDescription.getDefaultGraphURIs().forEach(builder::addUsingGraphURI);

        UpdateExecutionHTTP result = builder.build();

        return result;
    }
}
