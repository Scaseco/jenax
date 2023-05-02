package org.aksw.jena_sparql_api.lookup;

import java.util.concurrent.Callable;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jenax.arq.connection.link.QueryExecFactoryQuery;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public abstract class PaginatorQueryBase<T>
    implements ListPaginator<T>
{
    protected QueryExecFactoryQuery qef;
    protected Query query;

    protected abstract Flowable<T> obtainResultIterator(Callable<QueryExec> qe);


    public PaginatorQueryBase(QueryExecFactoryQuery qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

//    public <X> ListPaginator<X> map(Function<Binding, X> fn) {
//        //ListPaginator<X>
//        return null;
//    }

    @Override
    public Flowable<T> apply(Range<Long> range) {
        Query clonedQuery = QueryTransformOps.shallowCopy(query); // query.cloneQuery();
        range = Range.atLeast(0l).intersection(range);
        QueryUtils.applyRange(clonedQuery, range);


        Flowable<T> result = obtainResultIterator(() -> qef.create(clonedQuery)); // new IteratorResultSetBinding(qe.execSelect());

//        Stream<T> result = Streams.stream(it);
//        result.onClose(() -> qe.close());
        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Single<Range<Long>> result = SparqlRx.fetchCountQuery(qef, query, itemLimit, rowLimit);
        //Range<Long> result = ServiceUtils.fetchCountQuery(qef, query, itemLimit, rowLimit);
        return result;
    }
}
