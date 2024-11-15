package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.Objects;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.jenax.arq.util.exec.query.QueryExecAdapter;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

/**
 * Map all non-select sparql query types to select queries.
 *
 * Describe queries are currently unsupported.
 */
public abstract class QueryExecOverRowSet
    extends QueryExecBaseSelect
{

    /** QueryExec wrapper for just the row set. */
    public static class QueryExecOverRowSetInternal
        extends AutoCloseableWithLeakDetectionBase
        implements QueryExecAdapter {
        protected RowSet rowSet;

        public QueryExecOverRowSetInternal(RowSet rowSet) {
            super();
            this.rowSet = Objects.requireNonNull(rowSet);
        }

        @Override
        public RowSet select() {
            return rowSet;
        }

        @Override
        protected void closeActual() throws Exception {
            rowSet.close();
        }
    }

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

    @Override
    protected QueryExec doSelect(Query query) {
        RowSet rowSet = createRowSet(query);
        return new QueryExecOverRowSetInternal(rowSet);
    }

//    @Override
//    public DatasetGraph getDataset() {
//        return null;
//    }
//
//    @Override
//    public Context getContext() {
//        return null;
//    }

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
