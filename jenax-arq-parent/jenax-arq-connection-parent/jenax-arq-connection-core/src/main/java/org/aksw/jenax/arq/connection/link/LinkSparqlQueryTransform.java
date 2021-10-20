package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public class LinkSparqlQueryTransform
    extends LinkSparqlQueryDelegateBase
{
    protected Function<? super Query, ? extends Query> transform;

    public LinkSparqlQueryTransform(LinkSparqlQuery delegate, Function<? super Query, ? extends Query> transform) {
        super(delegate);
        this.transform = transform;
    }

    @Override
    public QueryExec query(Query query) {
        Query effectiveQuery = transform.apply(query);
        return getDelegate().query(effectiveQuery);
    }

    @Override
    public QueryExecBuilder newQuery() {
        return new QueryExecBuilderDelegateBaseQuery(delegate.newQuery())  {
            @Override
            public QueryExecBuilder query(Query query) {
                Query effectiveQuery = transform.apply(query);
                return super.query(effectiveQuery);
            }
        };
    }
}
