package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * An RdfDataSource wrapper for a dataset. The connection supplier is
 * a lambda in order to allow for context mutations and query transformations.
 *
 * @author raven
 *
 */
public class RdfDataEngineFromDataset
    implements RdfDataEngineWithDataset
{
    protected Dataset dataset;
    protected Function<? super Dataset, ? extends RDFConnection> connSupplier;
    protected Closeable closeAction;

    public RdfDataEngineFromDataset(
            Dataset dataset,
            Function<? super Dataset, ? extends RDFConnection> connSupplier,
            Closeable closeAction) {
        super();
        this.dataset = dataset;
        this.connSupplier = connSupplier;
        this.closeAction = closeAction;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public RDFConnection getConnection() {
        return connSupplier.apply(dataset);
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        }
    }

    public static RdfDataEngineFromDataset create(Dataset dataset, Function<? super Dataset, ? extends RDFConnection> connSupplier, Closeable closeAction) {
        return new RdfDataEngineFromDataset(dataset, connSupplier, closeAction);
    }

    public static RdfDataEngineFromDataset create(Dataset dataset, boolean closeDataset) {
        return new RdfDataEngineFromDataset(dataset, RDFConnection::connect, closeDataset ? dataset::close : null);
    }
}
