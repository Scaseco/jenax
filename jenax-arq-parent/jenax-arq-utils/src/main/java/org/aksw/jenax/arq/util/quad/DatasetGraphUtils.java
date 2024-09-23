package org.aksw.jenax.arq.util.quad;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.collections.diff.Diff;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;

import com.google.common.collect.Streams;

public class DatasetGraphUtils {
    public static long tupleCount(DatasetGraph dsg) {
        long result = Txn.calculateRead(dsg, () -> {
            long r = 0;
            Graph g = dsg.getDefaultGraph();
            if (g != null) {
                long contrib = g.sizeLong();
                r += contrib;
            }
            Iterator<Node> it = dsg.listGraphNodes();
            try {
                while (it.hasNext()) {
                    Node n = it.next();
                    Graph gg = dsg.getGraph(n);
                    long contrib = gg.sizeLong();
                    r += contrib;
                }
            } finally {
                Iter.close(it);
            }
            return r;
        });
        return result;
    }

    public static Stream<Node> streamNodes(DatasetGraph dg) {
        return Streams.stream(dg.find()).flatMap(QuadUtils::streamNodes);
    }

    public static Iterator<Node> iterateNodes(DatasetGraph dg) {
        return streamNodes(dg).iterator();
    }

    public static void addAll(DatasetGraph target, Node g, Graph source) {
        Iterator<Triple> it = source.find();
        while(it.hasNext()) {
            Triple t = it.next();
            target.add(new Quad(g, t));
        }
    }

    public static DatasetGraph addAll(DatasetGraph target, DatasetGraph source) {
        Iterator<Quad> it = source.find();
        addAll(target, it);
        return target;
    }

    public static DatasetGraph addAll(DatasetGraph datasetGraph, Iterable<? extends Quad> items) {
        addAll(datasetGraph, items.iterator());
        return datasetGraph;
    }

    public static DatasetGraph addAll(DatasetGraph datasetGraph, Iterator<? extends Quad> it) {
        while(it.hasNext()) {
            Quad q = it.next();
            datasetGraph.add(q);
        }

        return datasetGraph;
    }

    public static DatasetGraph clone(DatasetGraph datasetGraph) {
        Iterator<Quad> it = datasetGraph.find();
        DatasetGraph clone = DatasetGraphFactory.createGeneral();
        addAll(clone, it);

        return clone;
    }

    /**
     * Merges two mappings of Node-&gt;DatasetGraph
     * Maybe this util class is not exactly the best place where to put it
     *
     * @param result
     * @param other
     * @return
     */
    public static Map<Node, DatasetGraph> mergeInPlace(Map<Node, DatasetGraph> result, Map<Node, DatasetGraph> other) {
        for(Entry<Node, DatasetGraph> entry : other.entrySet()) {
            Node node = entry.getKey();
            DatasetGraph otherGraph = entry.getValue();
            DatasetGraph graph = result.get(node);
            if(graph == null) {
                graph = DatasetGraphFactory.createGeneral();
                result.put(node, graph);
            }

            DatasetGraphUtils.addAll(graph, otherGraph);
        }

        return result;
    }


    public static Diff<DatasetGraph> wrapDiffDatasetGraph(Diff<? extends Iterable<? extends Quad>> diff) {
        DatasetGraph added = DatasetGraphFactory.createGeneral();
        DatasetGraph removed = DatasetGraphFactory.createGeneral();

        DatasetGraphUtils.addAll(added, diff.getAdded());
        DatasetGraphUtils.addAll(removed, diff.getRemoved());


        Diff<DatasetGraph> result = new Diff<DatasetGraph>(added, removed, null);
        return result;
    }


    public static Graph getDefaultOrNamedGraph(DatasetGraph datasetGraph, Node graphName) {
        boolean isDefaultGraph = Quad.isDefaultGraph(graphName);

        Graph result = isDefaultGraph
            ? datasetGraph.getDefaultGraph()
            : datasetGraph.getGraph(graphName);

        return result;
    }


    public static void write(PrintStream out, DatasetGraph dg) {
        Dataset ds = DatasetFactory.wrap(dg);


        Model dm = ds.getDefaultModel();
        if(!dm.isEmpty()) {
            out.println("Begin of Default model -----------------------");
            dm.write(out, "TURTLE");
            out.println("End of Default model -----------------------");
        }
        Iterator<String> it = ds.listNames();
        while(it.hasNext()) {
            String name = it.next();
            Model model = ds.getNamedModel(name);
            System.out.println("Begin of " + name + " -----------------------");
            model.write(out, "TURTLE");
            System.out.println("End of " + name + " -----------------------");
        }

    }

}
