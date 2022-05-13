package org.aksw.jena_sparql_api.lookup;

import java.util.function.Function;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListPaginatorSparql
    implements ListPaginator<Binding>
{
    protected Query query;
    protected Function<? super Query, ? extends QueryExecution> qef;

    public ListPaginatorSparql(Query query, Function<? super Query, ? extends QueryExecution> qef) {
        super();
        this.query = query;
        this.qef = qef;
    }

    @Override
    public Flowable<Binding> apply(Range<Long> t) {
        // Query q = query.cloneQuery();
    	Query q = QueryTransformOps.shallowCopy(query);
        QueryUtils.applyRange(q, t);

        Flowable<Binding> result = SparqlRx.execSelectRaw(() -> qef.apply(q));
        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        return SparqlRx.fetchCountQuery(qef, query, itemLimit, rowLimit);
    }
}
