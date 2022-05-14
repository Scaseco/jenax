package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;


/**
 * LinkSparqlQuery wrapper that can transform both
 * the incoming Query and the obtained QueryExec instances.
 * Supplied transformation functions may be null.
 */
public class LinkSparqlQueryTransform
    extends LinkSparqlQueryDelegateBase
{
    protected Function<? super Query, ? extends Query> queryTransform;
    protected Function<? super QueryExec, ? extends QueryExec> queryExecTransform;

    public LinkSparqlQueryTransform(
            LinkSparqlQuery delegate,
            Function<? super Query, ? extends Query> transform,
            Function<? super QueryExec, ? extends QueryExec> queryExecTransform) {
        super(delegate);
        this.queryTransform = transform;
        this.queryExecTransform = queryExecTransform;
    }

    @Override
    public QueryExec query(Query query) {
        Query effectiveQuery = queryTransform == null
                ? query
                : queryTransform.apply(query);

        QueryExec qe = getDelegate().query(effectiveQuery);

        QueryExec result = queryExecTransform == null
                ? qe
                : queryExecTransform.apply(qe);

        return result;
    }

    @Override
    public QueryExecBuilder newQuery() {
        return QueryExecBuilderWrapperWithTransform.create(delegate.newQuery(), queryTransform, queryExecTransform);
    }
}
