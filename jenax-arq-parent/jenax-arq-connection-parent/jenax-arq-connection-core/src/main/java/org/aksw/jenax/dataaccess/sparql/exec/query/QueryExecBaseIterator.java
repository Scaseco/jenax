package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.graph.GraphFactory;

/** Mixin of {@link QueryExec} that provides iterator-based default implementations. */
public interface QueryExecBaseIterator
    extends QueryExec
{
    @Override
    default Graph construct(Graph result) {
        GraphUtil.add(result, constructTriples());
        return result;
    }

    @Override
    default Graph construct() {
        Graph result = GraphFactory.createDefaultGraph();
        construct(result);
        return result;
    }

    @Override
    default Graph describe(Graph result) {
        GraphUtil.add(result, describeTriples());
        return result;
    }

    @Override
    default Graph describe() {
        Graph result = GraphFactory.createDefaultGraph();
        construct(result);
        return result;
    }

    @Override
    default DatasetGraph constructDataset(DatasetGraph dataset) {
        constructQuads().forEachRemaining(dataset::add);
        return dataset;
    }

    @Override
    default DatasetGraph constructDataset() {
        DatasetGraph result = DatasetGraphFactory.create();
        constructDataset(result);
        return result;
    }

    @Override
    default JsonArray execJson() {
        JsonArray result = new JsonArray();
        execJsonItems().forEachRemaining(result::add);
        return result;
    }
}

