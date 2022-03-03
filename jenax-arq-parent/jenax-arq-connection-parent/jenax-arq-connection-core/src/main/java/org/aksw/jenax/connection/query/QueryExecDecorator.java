package org.aksw.jenax.connection.query;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

public interface QueryExecDecorator
    extends QueryExec
{
    QueryExec getDecoratee();

    default Context getContext() {
        return getDecoratee().getContext();
    }

    @Override
    default Query getQuery() {
        return getDecoratee().getQuery();
    }

    @Override
    default String getQueryString() {
        return getDecoratee().getQueryString();
    }

    @Override
    default void close() {
        getDecoratee().close();
    }

    @Override
    default boolean isClosed() {
        return getDecoratee().isClosed();
    }

    @Override
    default void abort() {
        getDecoratee().abort();
    }


    default void beforeExec() {

    }

    default void afterExec() {

    }

    default void onException(Exception e) {
    }

    @Override
    default RowSet select() {
        beforeExec();
        try {
            return getDecoratee().select();
        } catch(Exception e) {
            onException(e);
            throw e;
//        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph construct() {
        beforeExec();
        try {
            return getDecoratee().construct();
        } catch(Exception e) {
            onException(e);
            //throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph construct(Graph graph) {
        beforeExec();
        try {
            return getDecoratee().construct(graph);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph describe() {
        beforeExec();
        try {
            return getDecoratee().describe();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph describe(Graph graph) {
        beforeExec();
        try {
            return getDecoratee().describe(graph);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default boolean ask() {
        beforeExec();
        try {
            return getDecoratee().ask();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Triple> constructTriples() {
        beforeExec();
        try {
            return getDecoratee().constructTriples();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Triple> describeTriples() {
        beforeExec();
        try {
            return getDecoratee().describeTriples();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Quad> constructQuads() {
        beforeExec();
        try {
            return getDecoratee().constructQuads();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default DatasetGraph constructDataset() {
        beforeExec();
        try {
            return getDecoratee().constructDataset();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default DatasetGraph constructDataset(DatasetGraph dataset) {
        beforeExec();
        try {
            return getDecoratee().constructDataset(dataset);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default JsonArray execJson() {
        beforeExec();
        try {
            return getDecoratee().execJson();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<JsonObject> execJsonItems() {
        beforeExec();
        try {
            return getDecoratee().execJsonItems();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }


    @Override
    default DatasetGraph getDataset() {
        return getDecoratee().getDataset();
    }

}
