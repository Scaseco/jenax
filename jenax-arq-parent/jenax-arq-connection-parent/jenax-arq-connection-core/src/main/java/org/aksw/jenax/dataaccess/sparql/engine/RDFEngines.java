package org.aksw.jenax.dataaccess.sparql.engine;

import java.io.Closeable;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineDecorator;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceOverDatasetGraph;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Methods to construct {@link RDFEngine} instances.
 *
 * RDFEngine is an RDFLink / DatasetGraph level abstraction, so no methods
 * for presentation layer views (Dataset / RDFConnection) are provided here.
 */
public class RDFEngines {
    public static RDFEngine of(RDFLinkSource linkSource) {
        return new RDFEngineSimple(linkSource, null);
    }

    public static RDFEngine of(RDFLinkSource linkSource, AutoCloseable closeAction) {
        return new RDFEngineSimple(linkSource, closeAction);
    }

    public static RDFEngine of(DatasetGraph datasetGraph) {
        return of(datasetGraph, true);
    }

    public static RDFEngine of(DatasetGraph datasetGraph, boolean closeOnDelete) {
        RDFLinkSource linkSource = new RDFLinkSourceOverDatasetGraph(datasetGraph);
        Closeable closeAction = closeOnDelete
            ? datasetGraph::close
            : null;
        return of(linkSource, closeAction);
    }

    /** Start decoration of a given engine. */
    public static <X extends RDFEngine> RDFEngineDecorator<X> decorate(X baseEngine) {
        return new RDFEngineDecorator<>(baseEngine);
    }
}
