package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDatasetGraph
    extends UpdateExecutionFactoryParsingBase
{
    private Dataset dataset;

    public UpdateExecutionFactoryDatasetGraph(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, dataset);
        return result;
    }

}
