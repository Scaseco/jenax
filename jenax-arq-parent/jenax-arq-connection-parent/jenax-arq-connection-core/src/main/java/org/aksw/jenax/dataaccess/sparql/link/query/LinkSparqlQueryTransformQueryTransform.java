package org.aksw.jenax.dataaccess.sparql.link.query;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.apache.jena.rdflink.LinkSparqlQuery;

public class LinkSparqlQueryTransformQueryTransform
    implements LinkSparqlQueryTransform
{
    protected QueryTransform queryTransform;
    protected QueryExecTransform queryExecTransform;

    public LinkSparqlQueryTransformQueryTransform(QueryTransform queryTransform,
            QueryExecTransform queryExecTransform) {
        super();
        this.queryTransform = queryTransform;
        this.queryExecTransform = queryExecTransform;
    }

    public QueryTransform getQueryTransform() {
        return queryTransform;
    }

    public QueryExecTransform getQueryExecTransform() {
        return queryExecTransform;
    }

    @Override
    public LinkSparqlQuery apply(LinkSparqlQuery t) {
        return new LinkSparqlQueryQueryTransform(t, queryTransform, queryExecTransform);
    }
}
