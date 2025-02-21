package org.aksw.jenax.dataaccess.sparql.datasource;

import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;

public class RDFDataSourceOverDataset
    implements RDFDataSource
{
    protected Dataset dataset;
    protected Function<? super Dataset, ? extends RDFConnection> connectionFactory;

    public RDFDataSourceOverDataset(Dataset dataset) {
        this(dataset, RDFConnection::connect);
    }

    public Dataset getDataset() {
        return dataset;
    }

    public RDFDataSourceOverDataset(Dataset dataset, Function<? super Dataset, ? extends RDFConnection> connectionFactory) {
        super();
        this.dataset = Objects.requireNonNull(dataset);
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection conn = connectionFactory.apply(dataset);
        return conn;
    }
}
