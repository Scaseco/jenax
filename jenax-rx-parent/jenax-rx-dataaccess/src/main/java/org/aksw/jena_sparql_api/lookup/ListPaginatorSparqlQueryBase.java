package org.aksw.jena_sparql_api.lookup;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.impl.Concept;

/**
 * Paginator based on a concept.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class ListPaginatorSparqlQueryBase<T>
    implements ListPaginator<T>
{
    protected QueryExecutionFactoryQuery qef;
    protected Concept filterConcept;
    protected boolean isLeftJoin;

    public ListPaginatorSparqlQueryBase(QueryExecutionFactoryQuery qef, Concept filterConcept, boolean isLeftJoin) {
        this.qef = qef;
        this.filterConcept = filterConcept;
        this.isLeftJoin = isLeftJoin;
    }
}
