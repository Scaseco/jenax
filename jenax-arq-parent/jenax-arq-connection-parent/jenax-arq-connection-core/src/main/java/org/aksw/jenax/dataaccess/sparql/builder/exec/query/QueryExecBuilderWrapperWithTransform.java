package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/** QueryExecBuilder base class which parses query strings and delegates them to the object based method */
public class QueryExecBuilderWrapperWithTransform
    extends QueryExecBuilderWrapperBaseParse
{
    protected QueryTransform queryTransform;
    protected QueryExecTransform queryExecTransform;

    protected QueryExecBuilderWrapperWithTransform(
            QueryExecBuilder delegate,
            QueryTransform queryTransform,
            QueryExecTransform queryExecTransform) {
        super(delegate);
        this.queryTransform = queryTransform;
        this.queryExecTransform = queryExecTransform;
    }

    /**
     *
     * @param delegate
     * @param queryTransformer null for identity transformation
     * @param queryExecTransformer null for identity transformation
     * @return
     */
    public static QueryExecBuilder create(
            QueryExecBuilder delegate,
            QueryTransform queryTransform,
            QueryExecTransform queryExecTransform) {
        return new QueryExecBuilderWrapperWithTransform(delegate, queryTransform, queryExecTransform);
    }

    @Override
    public QueryExecBuilder query(Query query) {
        Query q = queryTransform == null
                ? query
                : queryTransform.apply(query);
        return super.query(q);
    }

    @Override
    public QueryExec build() {
        QueryExec raw = super.build();
        QueryExec result = queryExecTransform == null
                ? raw
                : queryExecTransform.apply(raw);
        return result;
    }

//    @Override
//    public QueryExecBuilder query(String queryString) {
//        Query query = QueryFactory.create(queryString);
//        return query(query);
//    }
//
//    @Override
//    public QueryExecBuilder query(String queryString, Syntax syntax) {
//        Query query = QueryFactory.create(queryString, syntax);
//        return query(query);
//    }
}
