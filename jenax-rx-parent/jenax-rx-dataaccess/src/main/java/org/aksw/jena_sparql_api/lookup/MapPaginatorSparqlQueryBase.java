package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;

public abstract class MapPaginatorSparqlQueryBase<K, V>
    extends ListPaginatorSparqlQueryBase<Entry<K, V>>
    implements MapPaginator<K, V>
{
    public MapPaginatorSparqlQueryBase(QueryExecutionFactoryQuery qef, Concept filterConcept, boolean isLeftJoin) {
        super(qef, filterConcept, isLeftJoin);
    }
}
