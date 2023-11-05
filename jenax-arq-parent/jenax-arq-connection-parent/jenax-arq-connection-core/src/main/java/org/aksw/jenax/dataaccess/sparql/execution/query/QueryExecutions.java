package org.aksw.jenax.dataaccess.sparql.execution.query;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;

public class QueryExecutions {
    /** Transform a QueryExecution based on an operation on the QueryExec level */
    public static QueryExecution transform(QueryExecution qef, QueryExecTransform transform) {
        QueryExec before = QueryExecAdapter.adapt(qef);
        QueryExec after = transform.apply(before);
        QueryExecution result = QueryExecutionAdapter.adapt(after);
        return result;
    }
}
