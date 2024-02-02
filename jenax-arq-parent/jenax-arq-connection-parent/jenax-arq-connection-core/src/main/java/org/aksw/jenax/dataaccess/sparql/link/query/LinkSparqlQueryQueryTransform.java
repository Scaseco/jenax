package org.aksw.jenax.dataaccess.sparql.link.query;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperWithTransform;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * LinkSparqlQuery wrapper that can transform both
 * the incoming Query and the obtained QueryExec instances.
 * Supplied transformation functions may be null.
 *
 * Note: Do not confuse with {@link LinkSparqlQueryTransform} which
 * is a function from LinkSparqlQuery to LinkSparqlQuery.
 */
public class LinkSparqlQueryQueryTransform
    extends LinkSparqlQueryWrapperBase
{
    protected QueryTransform queryTransform;
    protected QueryExecTransform queryExecTransform;

    public LinkSparqlQueryQueryTransform(
            LinkSparqlQuery delegate,
            QueryTransform transform,
            QueryExecTransform queryExecTransform) {
        super(delegate);
        this.queryTransform = transform;
        this.queryExecTransform = queryExecTransform;
    }

//    @Override
//    public QueryExec query(Query query) {
//        Query effectiveQuery = queryTransform == null
//                ? query
//                : queryTransform.apply(query);
//
//        QueryExec qe = getDelegate().query(effectiveQuery);
//
//        QueryExec result = queryExecTransform == null
//                ? qe
//                : queryExecTransform.apply(qe);
//
//        return result;
//    }

    @Override
    public QueryExecBuilder newQuery() {
        QueryExecBuilder delegateBuilder = delegate.newQuery();
        return QueryExecBuilderWrapperWithTransform.create(delegateBuilder, queryTransform, queryExecTransform);
    }
}
