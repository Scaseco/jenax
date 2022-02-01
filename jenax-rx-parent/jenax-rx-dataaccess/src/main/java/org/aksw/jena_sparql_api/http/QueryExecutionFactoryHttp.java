package org.aksw.jena_sparql_api.http;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBase;
import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBase
{
    private String service;
    private DatasetDescription datasetDescription;
    private HttpClient httpClient;

    //private List<String> defaultGraphs = new ArrayList<String>();

    public QueryExecutionFactoryHttp(String service) {
        this(service, Collections.<String>emptySet());
    }

    public QueryExecutionFactoryHttp(String service, String defaultGraphName) {
        this(service, defaultGraphName == null ? Collections.<String>emptySet() : Collections.singleton(defaultGraphName));
    }

    public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
        this(service, new DatasetDescription(new ArrayList<String>(defaultGraphs), Collections.<String>emptyList()), null);
    }

    public QueryExecutionFactoryHttp(String service, DatasetDescription datasetDescription, HttpClient httpClient) {
        this.service = service;
        this.datasetDescription = datasetDescription;
        this.httpClient = httpClient;
    }

    @Override
    public String getId() {
        return service;
    }

    @Override
    public String getState() {
        String result = DatasetDescriptionUtils.toString(datasetDescription);

            //TODO Include authenticator
        return result;
    }

    public QueryExecution postProcess(QueryExecutionHTTPBuilder builder) {
        datasetDescription.getNamedGraphURIs().forEach(builder::addNamedGraphURI);
        datasetDescription.getDefaultGraphURIs().forEach(builder::addDefaultGraphURI);

        QueryExecution result = builder.build();
        result = new QueryExecutionHttpWrapper(result);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryExecutionHTTPBuilder builder = QueryExecutionHTTPBuilder.create()
                .httpClient(httpClient)
                .endpoint(service)
                .query(queryString);
        QueryExecution result = postProcess(builder);

        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecutionHTTPBuilder builder = QueryExecutionHTTPBuilder.create()
                .httpClient(httpClient)
                .endpoint(service)
                .query(query);
        QueryExecution result = postProcess(builder);

        return result;
    }

}
