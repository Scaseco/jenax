package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * Base implementation of a QueryExecBuilderWrapper that immediately parses
 * query strings when creating a query exec builder.
 * Note, that query strings can still be passed to {@link QueryExecBuilder#query(String)} and
 * {@link QueryExecBuilder#query(String, Syntax)}.
 */
public class QueryExecBuilderDelegateBaseParse
    extends QueryExecBuilderWrapperBase
{
    public QueryExecBuilderDelegateBaseParse(QueryExecBuilder delegate) {
        super(delegate);
    }

    @Override
    public QueryExecBuilder query(String queryString) {
        Query query = QueryFactory.create(queryString);
        return query(query);
    }

    @Override
    public QueryExecBuilder query(String queryString, Syntax syntax) {
        Query query = QueryFactory.create(queryString, syntax);
        return query(query);
    }
}
