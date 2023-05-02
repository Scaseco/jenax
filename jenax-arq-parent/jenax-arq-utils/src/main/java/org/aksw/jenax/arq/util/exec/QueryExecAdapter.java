package org.aksw.jenax.arq.util.exec;

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
 * Adapter class in the sense thet most methods raise an UnsupportedOperationException
 * Can be used as a base class to provide limited behavior, such as only query(), select() and close().
 */
public class QueryExecAdapter
	implements QueryExec
{
	@Override
	public DatasetGraph getDataset() {
		return null;
	}

	@Override
	public Context getContext() {
		return null;
	}

	@Override
	public Query getQuery() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public void abort() {
	}

	@Override
	public void close() {
	}

	@Override
	public RowSet select() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Graph construct(Graph graph) {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Iterator<Triple> constructTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Iterator<Quad> constructQuads() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public DatasetGraph constructDataset(DatasetGraph dataset) {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Graph describe(Graph graph) {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Iterator<Triple> describeTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public boolean ask() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public JsonArray execJson() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public Iterator<JsonObject> execJsonItems() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

	@Override
	public boolean isClosed() {
        throw new UnsupportedOperationException("Not Implemented.");
	}

}
