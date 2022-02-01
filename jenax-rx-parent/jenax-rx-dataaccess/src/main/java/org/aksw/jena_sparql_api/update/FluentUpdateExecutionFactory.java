package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryHttp;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryModel;
import org.aksw.jenax.arq.connection.core.UpdateExecutionFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.util.Context;

import java.net.http.HttpClient;

public class FluentUpdateExecutionFactory {
    private UpdateExecutionFactory uef;

    public FluentUpdateExecutionFactory(UpdateExecutionFactory uef) {
        super();
        this.uef = uef;
    }

    public UpdateExecutionFactory create() {
        return uef;
    }


    public static FluentUpdateExecutionFactory from(UpdateExecutionFactory uef) {
        FluentUpdateExecutionFactory result = new FluentUpdateExecutionFactory(uef);
        return result;
    }

    public static FluentUpdateExecutionFactory from(Model model) {
        UpdateExecutionFactory uef = new UpdateExecutionFactoryModel(model);
        FluentUpdateExecutionFactory result = FluentUpdateExecutionFactory.from(uef);
        return result;
    }

    public static FluentUpdateExecutionFactory from(Model model, Context context) {
        DatasetGraph datasetGraph = DatasetGraphFactory.create(model.getGraph());
        FluentUpdateExecutionFactory result = from(datasetGraph, context);
        return result;
    }


    public static FluentUpdateExecutionFactory from(Dataset dataset) {
        FluentUpdateExecutionFactory result = from(dataset, null);
        return result;
    }

    public static FluentUpdateExecutionFactory from(Dataset dataset, Context context) {
        UpdateExecutionFactory uef = new UpdateExecutionFactoryDataset(dataset, context);
        FluentUpdateExecutionFactory result = FluentUpdateExecutionFactory.from(uef);
        return result;
    }

    public static FluentUpdateExecutionFactory from(DatasetGraph datasetGraph) {
        FluentUpdateExecutionFactory result = from(datasetGraph, null);
        return result;
    }

    public static FluentUpdateExecutionFactory from(DatasetGraph datasetGraph, Context context) {
        Dataset dataset = DatasetImpl.wrap(datasetGraph);
        FluentUpdateExecutionFactory result = from(dataset, context);
        return result;
    }


    public static FluentUpdateExecutionFactory http(String endpointUrl, DatasetDescription datasetDescription, HttpClient httpClient) {
        UpdateExecutionFactory uef = new UpdateExecutionFactoryHttp(endpointUrl, datasetDescription, httpClient);

        FluentUpdateExecutionFactory result = FluentUpdateExecutionFactory.from(uef);
        return result;
    }

}
