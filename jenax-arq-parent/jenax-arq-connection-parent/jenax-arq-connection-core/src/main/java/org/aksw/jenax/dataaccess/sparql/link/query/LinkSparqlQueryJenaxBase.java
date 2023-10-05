package org.aksw.jenax.dataaccess.sparql.link.query;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalDelegate;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * Provide a {@link LinkSparqlQuery} view over a {@link QueryExecutionFactoryQuery}.
 * The link does not support {@link #newQuery()}.
 */
public class LinkSparqlQueryJenaxBase<T extends QueryExecFactoryQuery>
    implements TransactionalDelegate, LinkSparqlQueryTmp
{
    protected T queryExecFactory;
    protected Transactional transactional;

    public LinkSparqlQueryJenaxBase(T queryExecutionFactory) {
        this(queryExecutionFactory, new TransactionalNull());
    }

    public LinkSparqlQueryJenaxBase(T queryExecutionFactory, Transactional transactional) {
        super();
        this.queryExecFactory = queryExecutionFactory;
        this.transactional = transactional;
    }

    @Override
    public Transactional getDelegate() {
        return transactional;
    }

    @Override
    public QueryExec query(Query query) {
        QueryExec result = queryExecFactory.create(query);
        return result;
    }

    @Override
    public void close() {
    }

    @Override
    public QueryExecBuilder newQuery() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
