package org.aksw.jena_sparql_api.lookup;

import java.util.concurrent.Callable;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExec;

import io.reactivex.rxjava3.core.Flowable;

public class PaginatorQueryTriple
    extends PaginatorQueryBase<Triple>
{

    public PaginatorQueryTriple(QueryExecFactoryQuery qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Flowable<Triple> obtainResultIterator(Callable<QueryExec> qeSupplier) {
        Flowable<Triple> result = SparqlRx.constructTriples(qeSupplier);
        return result;
    }
}

