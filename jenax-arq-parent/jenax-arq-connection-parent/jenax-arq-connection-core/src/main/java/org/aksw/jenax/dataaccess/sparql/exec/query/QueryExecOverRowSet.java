package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

/**
 * Map all non-select sparql query types to select queries.
 *
 * Describe queries are currently unsupported.
 */
public abstract class QueryExecOverRowSet
    extends QueryExecBaseSelect
{
    protected RowSet activeRowSet = null;

    public QueryExecOverRowSet(Query query) {
        super(query);
    }

    protected synchronized void setActiveRowSet(RowSet rowSet) {
        this.activeRowSet = rowSet;

        if (isClosed()) {
            activeRowSet.close();
        }
    }

    protected RowSet getActiveRowSet() {
        return activeRowSet;
    }

    /**
     * The actual method that needs to be implemented. The argument is always a SPARQL
     * query of select type.
     * The RowSet may need to implement close() in order to close an underlying query execution.
     */
    protected abstract RowSet createRowSet(Query selectQuery);

    protected synchronized final RowSet doSelect(Query query) {
        ensureOpen();

        if(this.getActiveRowSet() != null) {
            throw new RuntimeException("A query is already running");
        }

        RowSet rowSet = createRowSet(query);

        if(rowSet == null) {
            throw new RuntimeException("Failed to obtain a QueryExecution for query: " + query);
        }

        setActiveRowSet(activeRowSet);
        return rowSet;
    }

    @Override
    public DatasetGraph getDataset() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void closeActual() {
        RowSet rowSet = getActiveRowSet();
        if (rowSet != null) {
            rowSet.close();
        }
    }

    @Override
    public void abort() {
        close();
    }
}
