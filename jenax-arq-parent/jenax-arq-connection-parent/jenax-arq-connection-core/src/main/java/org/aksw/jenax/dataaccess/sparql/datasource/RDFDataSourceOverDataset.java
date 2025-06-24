package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;

public class RDFDataSourceOverDataset
    implements RDFDataSource
{
    protected Dataset dataset;

    public RDFDataSourceOverDataset(Dataset dataset) {
        super();
        this.dataset = Objects.requireNonNull(dataset);
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection result = RDFConnection.connect(dataset);
        return result;
    }
}
