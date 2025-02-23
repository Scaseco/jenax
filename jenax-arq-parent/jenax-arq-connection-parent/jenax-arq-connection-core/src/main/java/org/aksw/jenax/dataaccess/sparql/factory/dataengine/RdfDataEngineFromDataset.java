package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceOverDatasetGraph;
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
    protected RDFLinkSource linkSource;
    protected boolean closeDataset;

    public RdfDataEngineFromDataset(
            DatasetGraph datasetGraph,
            boolean closeDataset) {
        super();
        this.linkSource = new RDFLinkSourceOverDatasetGraph(datasetGraph);
        this.closeDataset = closeDataset;
    }

    @Override
    public RDFLinkSource getLinkSource() {
        return linkSource;
    }

    @Override
    public void close() throws Exception {
        if (closeDataset) {
            linkSource.getDatasetGraph().close();
        }
    }

//    public static RdfDataEngineFromDataset create(
//            Dataset dataset, Function<? super Dataset, ? extends RDFConnection> connectionFactory, Closeable closeAction)
//    {
//        RDFDataSourceOverDataset dataSource = new RDFDataSourceOverDataset(dataset, connectionFactory);
//        return new RdfDataEngineFromDataset(dataSource, closeAction);
//    }

    public static RdfDataEngineFromDataset create(DatasetGraph datasetGraph, boolean closeDataset) {
        return new RdfDataEngineFromDataset(datasetGraph, closeDataset);
        // return create(dataset, RDFConnection::connect, closeDataset ? dataset::close : null);
    }

    /** Create an engine whose close method closes the given dataset. */
    public static RdfDataEngineFromDataset create(DatasetGraph dataset) {
        return create(dataset, true);
    }
}
