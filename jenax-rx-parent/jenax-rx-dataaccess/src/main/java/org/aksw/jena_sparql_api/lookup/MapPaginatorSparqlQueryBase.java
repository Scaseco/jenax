package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment1;

public abstract class MapPaginatorSparqlQueryBase<K, V>
    extends ListPaginatorSparqlQueryBase<Entry<K, V>>
    implements MapPaginator<K, V>
{
    public MapPaginatorSparqlQueryBase(QueryExecutionFactoryQuery qef, Fragment1 filterConcept, boolean isLeftJoin) {
        super(qef, filterConcept, isLeftJoin);
    }
}
