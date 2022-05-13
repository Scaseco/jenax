package org.apache.jena.sparql.exec;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * In jena's package because QueryExecutionAdapter.get is protected
 *
 * Fixes NPE on getDataset
 */
public class QueryExecAdapterFix
	extends QueryExecAdapter
{
	protected QueryExecAdapterFix(QueryExecution queryExecution) {
		super(queryExecution);
	}

    public static QueryExec adapt(QueryExecution qExec) {
        if ( qExec instanceof QueryExecutionAdapter) {
            return ((QueryExecutionAdapter)qExec).get();
        }
        return new QueryExecAdapterFix(qExec);
    }

    @Override
    public DatasetGraph getDataset() {
        Dataset ds = get().getDataset();
        DatasetGraph result = ds == null ? null : ds.asDatasetGraph();
        return result;
    }
}
