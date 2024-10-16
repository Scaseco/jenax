package org.aksw.jenax.arq.util.io;

import java.io.OutputStream;
import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Interface for RDF data generators that can emit to StreamRDF.
 * This class adds convenience methods to obtain graphs, models, dataset graphs and datasets.
 */
public interface StreamRDFEmitter
    extends ToGraph, ToDatasetGraph
{
    void emit(StreamRDF streamRDF);

    default void output(OutputStream out, Lang lang) {
        StreamRDF streamRDF = StreamRDFWriter.getWriterStream(out, lang);
        streamRDF.start();
        emit(streamRDF);
        streamRDF.finish();
    }

    @Override
    default Graph toGraph(Graph graph) {
        return toGraph(graph, this);
    }

    default Graph toGraph(Graph destination, StreamRDFEmitter emitter) {
        Objects.requireNonNull(destination);
        StreamRDF sink = StreamRDFLib.graph(destination);
        sink.start();
        emitter.emit(sink);
        sink.finish();
        return destination;
    }

    @Override
    default DatasetGraph toDatasetGraph(DatasetGraph datasetGraph) {
        return toDatasetGraph(datasetGraph, this);
    }

    default DatasetGraph toDatasetGraph(DatasetGraph destination, StreamRDFEmitter emitter) {
        Objects.requireNonNull(destination);
        StreamRDF sink = StreamRDFLib.dataset(destination);
        sink.start();
        emitter.emit(sink);
        sink.finish();
        return destination;
    }
}
