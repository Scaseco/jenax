package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceOverDataset;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLinkDatasetBuilder;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * An RdfDataSource wrapper for a dataset. The connection supplier is
 * a lambda in order to allow for context mutations and query transformations.
 *
 * @author raven
 *
 */
public class RdfDataEngineFromDataset
    implements RDFEngine
{
    protected RDFDataSourceOverDataset dataSource;
    protected Closeable closeAction;

    public RdfDataEngineFromDataset(
            RDFDataSourceOverDataset dataSource,
            Closeable closeAction) {
        super();
        this.dataSource = dataSource;
        this.closeAction = closeAction;
    }

    @Override
    public RDFLinkBuilder newLinkBuilder() {
        return new RDFLinkBuilderOverLinkSupplier(() ->
            RDFLinkDatasetBuilder.newBuilder()
                .dataset(dataSource.getDataset().asDatasetGraph())
                .build());
    }

    @Override
    public DatasetGraph getDataset() {
        return dataSource.getDataset().asDatasetGraph();
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        }
    }

    public static RdfDataEngineFromDataset create(
            Dataset dataset, Function<? super Dataset, ? extends RDFConnection> connectionFactory, Closeable closeAction)
    {
        RDFDataSourceOverDataset dataSource = new RDFDataSourceOverDataset(dataset, connectionFactory);
        return new RdfDataEngineFromDataset(dataSource, closeAction);
    }

    public static RdfDataEngineFromDataset create(Dataset dataset, boolean closeDataset) {
        return create(dataset, RDFConnection::connect, closeDataset ? dataset::close : null);
    }

    /** Create an engine whose close method closes the given dataset. */
    public static RdfDataEngineFromDataset create(Dataset dataset) {
        return create(dataset, true);
    }
}
