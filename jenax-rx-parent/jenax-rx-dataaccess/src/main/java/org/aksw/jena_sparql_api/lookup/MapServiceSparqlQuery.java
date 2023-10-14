package org.aksw.jena_sparql_api.lookup;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;


public class MapServiceSparqlQuery
    implements MapService<Concept, Node, Table>
{
    protected QueryExecutionFactoryQuery qef;
    protected boolean isLeftJoin;

    protected Query attrQuery;
    protected Var attrVar;
    protected boolean forceSubQuery;

    public MapServiceSparqlQuery(QueryExecutionFactoryQuery qef, Query attrQuery, Var attrVar) {
        this(qef, attrQuery, attrVar, true, false);
    }

    public MapServiceSparqlQuery(QueryExecutionFactoryQuery qef, Query attrQuery, Var attrVar, boolean isLeftJoin) {
        this(qef, attrQuery, attrVar, isLeftJoin, false);
    }

    public MapServiceSparqlQuery(QueryExecutionFactoryQuery qef, Query attrQuery,
            Var attrVar, boolean isLeftJoin, boolean forceSubQuery) {
        super();
        this.qef = qef;
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.isLeftJoin = isLeftJoin;
        this.forceSubQuery = forceSubQuery;
    }

    @Override
    public MapPaginator<Node, Table> createPaginator(Concept filterConcept) {
        MapPaginatorSparqlQuery result = new MapPaginatorSparqlQuery(qef, filterConcept, isLeftJoin, attrQuery, attrVar, forceSubQuery);
        return result;
    }

}
