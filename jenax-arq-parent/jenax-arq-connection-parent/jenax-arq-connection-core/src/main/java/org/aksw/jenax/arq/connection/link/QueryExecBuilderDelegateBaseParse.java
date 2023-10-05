package org.aksw.jenax.arq.connection.link;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public class QueryExecBuilderDelegateBaseParse
    extends QueryExecBuilderDelegateBase
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
