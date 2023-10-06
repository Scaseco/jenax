package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;


/**
 * Execute non-select query forms as select queries.
 *
 * {@inheritDoc}
 */
public class QueryExecSelect
    extends QueryExecBaseSelect
    implements QueryExecDecorator
{
    protected QueryExec delegate;

    protected QueryExecSelect(Query query, QueryExec delegate) {
        super(query);
        this.delegate = delegate;
    }

    /**
     * Create a QueryExec
     *
     * @param query The query which should be executed as a select one
     * @param qef A query exec factory that receives the select query derived from 'query'
     * @return
     */
    public static QueryExec of(Query query, QueryExecFactoryQuery qef) {
        Query selectQuery = QueryExecBaseSelect.adjust(query);
        QueryExec actualExec = qef.create(selectQuery);
        return new QueryExecSelect(query, actualExec);
    }

    @Override
    protected RowSet doSelect(Query selectQuery) {
        return getDecoratee().select();
    }

    @Override
    public QueryExec getDecoratee() {
        return delegate;
    }
}
