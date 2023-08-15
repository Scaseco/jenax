package org.aksw.jenax.arq.connection.link;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.sparql.core.DatasetGraph;

public interface LinkDatasetGraphDelegate
    extends LinkDatasetGraphTmp
{
    @Override
    LinkDatasetGraph getDelegate();

    @Override
    default void load(Node graphName, String file) {
        getDelegate().load(graphName, file);
    }

    @Override
    default void load(String file) {
        getDelegate().load(file);
    }

    @Override
    default void load(Node graphName, Graph graph) {
        getDelegate().load(graphName, graph);
    }

    @Override
    default void load(Graph graph) {
        getDelegate().load(graph);
    }

    @Override
    default void put(Node graphName, String file) {
        getDelegate().put(graphName, file);
    }

    @Override
    default void put(String file) {
        getDelegate().put(file);
    }

    @Override
    default void put(Node graphName, Graph graph) {
        getDelegate().put(graphName, graph);
    }

    @Override
    default void put(Graph graph) {
        getDelegate().put(graph);
    }

    @Override
    default void delete(Node graphName) {
        getDelegate().delete(graphName);
    }

    @Override
    default void delete() {
        getDelegate().delete();
    }

    @Override
    default void loadDataset(String file) {
        getDelegate().loadDataset(file);
    }

    @Override
    default void loadDataset(DatasetGraph dataset) {
        getDelegate().loadDataset(dataset);
    }

    @Override
    default void putDataset(String file) {
        getDelegate().putDataset(file);
    }

    @Override
    default void putDataset(DatasetGraph dataset) {
        getDelegate().putDataset(dataset);
    }

    @Override
    default void clearDataset() {
        getDelegate().clearDataset();
    }

    @Override
    default boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default Graph get(Node graphName) {
        return getDelegate().get(graphName);
    }

    @Override
    default Graph get() {
        return getDelegate().get();
    }

    @Override
    default DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }
}
