package org.aksw.jenax.dataaccess.sparql.link.common;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalDelegate;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.UpdateRequest;

public interface RDFLinkDelegate
    extends RDFLink, TransactionalDelegate
{
    @Override
    RDFLink getDelegate();

    @Override
    default DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }

    @Override
    default QueryExec query(Query query) {
        return getDelegate().query(query);
    }

    @Override
    default QueryExecBuilder newQuery() {
        return getDelegate().newQuery();
    }

    @Override
    default UpdateExecBuilder newUpdate() {
        return getDelegate().newUpdate();
    }

    @Override
    default void update(UpdateRequest update) {
        getDelegate().update(update);
    }

    @Override
    default Graph get() {
        return getDelegate().get();
    }

    @Override
    default Graph get(Node graphName) {
        return getDelegate().get(graphName);
    }

    @Override
    default void load(String file) {
        getDelegate().load(file);
    }

    @Override
    default void load(Node graphName, String file) {
        getDelegate().load(graphName, file);
    }

    @Override
    default void load(Graph graph) {
        getDelegate().load(graph);
    }

    @Override
    default void load(Node graphName, Graph graph) {
        getDelegate().load(graphName, graph);
    }

    @Override
    default void put(String file) {
        getDelegate().put(file);
    }

    @Override
    default void put(Node graphName, String file) {
        getDelegate().put(graphName, file);
    }

    @Override
    default void put(Graph graph) {
        getDelegate().put(graph);
    }

    @Override
    default void put(Node graphName, Graph graph) {
        getDelegate().put(graphName, graph);
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
}
