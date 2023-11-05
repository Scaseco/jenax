package org.aksw.jena_sparql_api.lookup;

import java.util.concurrent.Callable;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;

import io.reactivex.rxjava3.core.Flowable;

public class PaginatorQueryBinding
    extends PaginatorQueryBase<Binding>
{

    public PaginatorQueryBinding(QueryExecFactoryQuery qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Flowable<Binding> obtainResultIterator(Callable<QueryExec> qeSupplier) {
        Flowable<Binding> result = SparqlRx.select(qeSupplier);

//        ResultSet rs = qe.execSelect();
//        Iterator<Binding> result = new IteratorResultSetBinding(rs);
        return result;
    }
}
