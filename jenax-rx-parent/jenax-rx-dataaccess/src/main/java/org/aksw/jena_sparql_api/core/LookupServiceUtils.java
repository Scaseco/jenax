package org.aksw.jena_sparql_api.core;

import java.util.List;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.rx.lookup.LookupServiceTransformValue;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.analytics.core.MappedQuery;
import org.aksw.jenax.arq.aggregation.Agg;
import org.aksw.jenax.arq.aggregation.AggList;
import org.aksw.jenax.arq.aggregation.AggLiteral;
import org.aksw.jenax.arq.aggregation.BindingMapperProjectVar;
import org.aksw.jenax.arq.aggregation.FunctionResultSetAggregate;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;

public class LookupServiceUtils {
//
//    public static <T> LookupService<Node, T> createLookupService2(QueryExecutionFactory sparqlService, MappedConcept<? extends Map<? extends Node, T>> mappedConcept) {
//        Concept concept = mappedConcept.getConcept();
//        Query query = concept.asQuery();
//        query.setQueryResultStar(true);
//
//        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator
//
//        LookupService<Node, ResultSetPart> base = new LookupServiceSparqlQuery(sparqlService, query, concept.getVar());
//
//        FunctionResultSetAggregate<? extends Map<? extends Node, T>> t1 = FunctionResultSetAggregate.create(mappedConcept.getAggregator());
//
//
//
//        Function<Map<? extends Node, T>, Map<Node, T>> fn = new Function<Map<? extends Node, T>, Map<Node, T>>() {
//
//            @Override
//            public Map<Node, T> apply(Map<? extends Node, T> input) {
//
//            }
//        };
//
//
//        LookupService<Node, T> result = LookupServiceTransformValue.create(base, transform);
//        return result;
//    }
    public static <T> LookupService<Node, List<Node>> createLookupService(QueryExecutionFactoryQuery qef, Fragment2 relation) {
        Var sourceVar = relation.getSourceVar();

        AggList<Node> agg = AggList.create(AggLiteral.create(BindingMapperProjectVar.create(relation.getTargetVar())));
        Query query = FragmentUtils.createQuery(relation);
        MappedQuery<List<Node>> mappedQuery = MappedQuery.create(query, sourceVar, agg);
        LookupService<Node, List<Node>> result = LookupServiceUtils.createLookupService(qef, mappedQuery);

        return result;
    }


    public static <T> LookupService<Node, T> createLookupService(QueryExecutionFactoryQuery sparqlService, MappedQuery<T> mappedQuery) {
        PartitionedQuery1 partQuery = mappedQuery.getPartQuery();
        Query query = partQuery.getQuery();
        Var partVar = partQuery.getPartitionVar();
        Agg<T> agg = mappedQuery.getAgg();

        LookupService<Node, Table> base = new LookupServiceSparqlQuery(sparqlService, query, partVar);
        FunctionResultSetAggregate<T> transform = FunctionResultSetAggregate.create(agg);
        LookupService<Node, T> result = LookupServiceTransformValue.create(base, transform);
        return result;
    }



    /**
     * This version is broken - use
     * MapServiceResourceShape.createLookupService(qef, shape);
     * @param sparqlService
     * @param mappedConcept
     * @return
     */
    @Deprecated
    public static <T> LookupService<Node, T> createLookupService(QueryExecutionFactoryQuery sparqlService, MappedConcept<T> mappedConcept) {

        if(true) {
            throw new RuntimeException("This method is broken. Use MapServiceResourceShape.createLookupService(qef, shape) instead)");
        }
        Concept concept = mappedConcept.getConcept();
        Query query = concept.asQuery();
        query.setQueryResultStar(true);

        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator

        LookupService<Node, Table> base = new LookupServiceSparqlQuery(sparqlService, query, concept.getVar());

        FunctionResultSetAggregate<T> transform = FunctionResultSetAggregate.create(mappedConcept.getAggregator());

        LookupService<Node, T> result = LookupServiceTransformValue.create(base, transform);
        return result;
    }
}
