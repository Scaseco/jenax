package org.aksw.jenax.dataaccess.sparql.link.dataset;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.sparql.core.DatasetGraph;

public interface LinkDatasetGraphWrapper
    extends LinkDatasetGraphBase
{
    @Override
    LinkDatasetGraph getDelegate();

    default void beforeExec() {
    }

    default void afterExec() {
    }

    default void onException(Exception e) {
    }

    @Override
    default void load(Node graphName, String file) {
        beforeExec();
        try {
            getDelegate().load(graphName, file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void load(String file) {
        try {
            getDelegate().load(file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void load(Node graphName, Graph graph) {
        try {
            getDelegate().load(graphName, graph);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void load(Graph graph) {
        try {
            getDelegate().load(graph);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void put(Node graphName, String file) {
        try {
            getDelegate().put(graphName, file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void put(String file) {
        try {
            getDelegate().put(file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void put(Node graphName, Graph graph) {
        try {
            getDelegate().put(graphName, graph);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void put(Graph graph) {
        try {
            getDelegate().put(graph);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void delete(Node graphName) {
        try {
            getDelegate().delete(graphName);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void delete() {
        try {
            getDelegate().delete();
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void loadDataset(String file) {
        try {
            getDelegate().loadDataset(file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void loadDataset(DatasetGraph dataset) {
        try {
            getDelegate().loadDataset(dataset);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void putDataset(String file) {
        try {
            getDelegate().putDataset(file);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void putDataset(DatasetGraph dataset) {
        try {
            getDelegate().putDataset(dataset);
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default void clearDataset() {
        try {
            getDelegate().clearDataset();
        } catch (Exception e) {
            onException(e);
        } finally {
            afterExec();
        }
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
    default Graph get(String graphName) {
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
