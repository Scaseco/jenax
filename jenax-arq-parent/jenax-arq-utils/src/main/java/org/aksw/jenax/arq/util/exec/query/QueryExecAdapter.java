package org.aksw.jenax.arq.util.exec.query;

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

/**
 * Adapter mixin where most methods raise an UnsupportedOperationException
 * Can be used as a base class to provide limited behavior, such as only query(), select() and close().
 */
public interface QueryExecAdapter
    extends QueryExec
{
    @Override
    default DatasetGraph getDataset() {
        return null;
    }

    @Override
    default Context getContext() {
        return null;
    }

    @Override
    default Query getQuery() {
        return null;
    }

    @Override
    default String getQueryString() {
        return null;
    }

    @Override
    default void abort() {
    }

    @Override
    default void close() {
    }

    @Override
    default RowSet select() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Graph construct(Graph graph) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Iterator<Triple> constructTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Iterator<Quad> constructQuads() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default DatasetGraph constructDataset(DatasetGraph dataset) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Graph describe(Graph graph) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Iterator<Triple> describeTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default boolean ask() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default JsonArray execJson() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default Iterator<JsonObject> execJsonItems() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    default boolean isClosed() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

}
