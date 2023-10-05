package org.aksw.jena_sparql_api.lookup;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryQuery;

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
