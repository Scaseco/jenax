package org.aksw.jena_sparql_api.lookup;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactories;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListPaginatorSparql
    implements ListPaginator<Binding>
{
    protected Query query;
    protected QueryExecFactoryQuery qef; // Function<? super Query, ? extends QueryExecution> qef;

    public ListPaginatorSparql(Query query, QueryExecutionFactoryQuery qef) {
    	this(query, QueryExecFactories.adapt(qef));
    }

    public ListPaginatorSparql(Query query, QueryExecFactoryQuery qef) {
        super();
        this.query = query;
        this.qef = qef;
    }

    @Override
    public Flowable<Binding> apply(Range<Long> t) {
        // Query q = query.cloneQuery();
    	Query q = QueryTransformOps.shallowCopy(query);
        QueryUtils.applyRange(q, t);

        Flowable<Binding> result = SparqlRx.select(qef, q);
        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        return SparqlRx.fetchCountQuery(qef, query, itemLimit, rowLimit);
    }
}
