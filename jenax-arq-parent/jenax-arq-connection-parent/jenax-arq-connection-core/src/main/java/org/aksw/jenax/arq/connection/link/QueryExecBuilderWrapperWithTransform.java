package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/** QueryExecBuilder base class which parses query strings and delegates them to the object based method*/
public class QueryExecBuilderWrapperWithTransform
    extends QueryExecBuilderDelegateBase
{
    protected Function<? super Query, ? extends Query> queryTransformer;
    protected Function<? super QueryExec, ? extends QueryExec> queryExecTransformer;

    protected QueryExecBuilderWrapperWithTransform(
            QueryExecBuilder delegate,
            Function<? super Query, ? extends Query> queryTransformer,
            Function<? super QueryExec, ? extends QueryExec> queryExecTransformer) {
        super(delegate);
        this.queryTransformer = queryTransformer;
        this.queryExecTransformer = queryExecTransformer;
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
            Function<? super Query, ? extends Query> queryTransformer,
            Function<? super QueryExec, ? extends QueryExec> queryExecTransformer) {
        return new QueryExecBuilderWrapperWithTransform(delegate, queryTransformer, queryExecTransformer);
    }

    @Override
    public QueryExecBuilder query(Query query) {
        Query q = queryTransformer == null
                ? query
                : queryTransformer.apply(query);
        return super.query(q);
    }

    @Override
    public QueryExec build() {
        QueryExec raw = super.build();
        QueryExec result = queryExecTransformer == null
                ? raw
                : queryExecTransformer.apply(raw);
        return result;
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
