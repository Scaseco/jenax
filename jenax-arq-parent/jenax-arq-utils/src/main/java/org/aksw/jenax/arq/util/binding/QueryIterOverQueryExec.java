package org.aksw.jenax.arq.util.binding;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

public class QueryIterOverQueryExec
    extends QueryIter
{
    protected QueryExec queryExec;
    protected RowSet rowSet;

    public QueryIterOverQueryExec(ExecutionContext execCxt, QueryExec queryExec) {
        super(execCxt);
        this.queryExec = queryExec;
    }

    @Override
    protected boolean hasNextBinding() {
        if (rowSet == null) {
            rowSet = queryExec.select();
        }

        return rowSet.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
//        if (rowSet == null) {
//            rowSet = queryExec.select();
//        }

        return rowSet.next();
    }

    @Override
    protected void closeIterator() {
        queryExec.close();
    }

    @Override
    protected void requestCancel() {
        queryExec.abort();
    }
}
