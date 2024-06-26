package org.aksw.jenax.dataaccess.sparql.connection.query;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalWrapper;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

/**
 * A query connection wrapper which can raise intentional exceptions on query execution.
 * Useful for debugging behavior of application code under failure.
 *
 * @author raven
 *
 */
public class SparqlQueryConnectionWithExecFails
    implements TransactionalWrapper, SparqlQueryConnectionTmp
{
    protected SparqlQueryConnection delegate;
    protected Function<? super Query, ? extends Throwable> queryToThrowable;

    public SparqlQueryConnectionWithExecFails(SparqlQueryConnection delegate,
            Function<? super Query, ? extends Throwable> queryToThrowable) {
        super();
        this.delegate = delegate;
        this.queryToThrowable = queryToThrowable;
    }

    @Override
    public SparqlQueryConnection getDelegate() {
        return delegate;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution core = getDelegate().query(query);
        return new QueryExecutionWithExecFails(core);
    }

    @Override
    public void close() {
    }


    public class QueryExecutionWithExecFails
        extends QueryExecutionWrapperBase<QueryExecution>
    {
        public QueryExecutionWithExecFails(QueryExecution delegate) {
            super(delegate);
            Objects.requireNonNull(delegate.getQuery(), "The delegate query execution must expose the query");
        }

        @Override
        protected void beforeExec() {
            super.beforeExec();

            Query query = getQuery();
            Throwable throwable = queryToThrowable.apply(query);

            if (throwable != null) {
                 throw new RuntimeException(throwable);
            }
        }
    }


    @Override
    public QueryExecutionBuilder newQuery() {
        throw new UnsupportedOperationException();
    }

}
