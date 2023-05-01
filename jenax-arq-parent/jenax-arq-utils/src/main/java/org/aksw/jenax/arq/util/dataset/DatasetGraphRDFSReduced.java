package org.aksw.jenax.arq.util.dataset;

import java.util.function.Function;

import org.aksw.commons.tuple.finder.TupleFinder3;
import org.aksw.jenax.arq.util.tuple.adapter.GraphOverTupleFinder3;
import org.aksw.jenax.arq.util.tuple.adapter.TupleFinder3OverGraph;
import org.aksw.jenax.arq.util.tuple.impl.MatchRDFSReduced;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.DatasetGraphRDFS;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.engine.Mappers;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * This class provides static methods to wrap a DatasetGraph with RDFS reasoning that improves over
 * {@link DatasetGraphRDFS}. This class is not a DatasetGraph by itself.
 */
public class DatasetGraphRDFSReduced {
    public static Function<Graph, Graph> asGraphTransform(Function<TupleFinder3<Triple, Node>, TupleFinder3<Triple, Node>> tupleFinderTransform) {
        return graph -> {
            TupleFinder3<Triple, Node> in = TupleFinder3OverGraph.wrap(graph);
            TupleFinder3<Triple, Node> out = tupleFinderTransform.apply(in);
            Graph r = GraphOverTupleFinder3.wrap(out);
            return r;
        };
    }

    public static DatasetGraph wrap(DatasetGraph dsg, ConfigRDFS<Node> setup) {
        Function<Graph, Graph> graphTransform = asGraphTransform(tf -> MatchRDFSReduced.create(setup, Mappers.mapperTriple(), tf));
        return new DatasetGraphWithGraphTransform(dsg, graphTransform);
    }

    public static DatasetGraph wrap(DatasetGraph dsg, Graph vocab) {
        ConfigRDFS<Node> setup = RDFSFactory.setupRDFS(vocab);
        return wrap(dsg, setup);
    }
}
