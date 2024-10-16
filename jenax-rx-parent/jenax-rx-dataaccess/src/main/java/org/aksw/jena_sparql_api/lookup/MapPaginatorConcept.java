package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * TODO Convert to a ListService
 *
 * @author raven
 *
 */
public class MapPaginatorConcept
    implements MapPaginator<Node, Node>
{
    protected QueryExecutionFactory qef;
    protected Fragment1 concept;

    public MapPaginatorConcept(QueryExecutionFactory qef, Fragment1 concept) {
        this.qef = qef;
        this.concept = concept;
    }

//    @Override
//    public Map<Node, Node> fetchMap(Range<Long> range) {
//        Query query = concept.asQuery();
//        QueryUtils.applyRange(query, range);
////        query.setLimit(limit == null ? Query.NOLIMIT : limit);
////        query.setOffset(offset == null ? Query.NOLIMIT : offset);
//
//        List<Node> tmp = QueryExecutionUtils.executeList(qef, query, concept.getVar());
//
//        //List<Entry<Node, Node>> result = new ArrayList<Entry<Node, Node>>(tmp.size());
//        Map<Node, Node> result = new LinkedHashMap<Node, Node>();
//        for(Node node : tmp) {
//            //Entry<Node, Node> item = Pair.create(node, node);
//            result.put(node, node);
//        }
//
//
//        return result;
//    }

    public static Query createSubQuery(Query query, Var var) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(var);
        result.setQueryPattern(esq);

        return result;
    }

    @Deprecated // Use QueryGeneration utils
    public static Query createQueryCount(Fragment1 concept, Long itemLimit, Long rowLimit, Var resultVar) {
        Query subQuery = concept.asQuery();

        if(rowLimit != null) {
            subQuery.setDistinct(false);
            subQuery.setLimit(rowLimit);

            subQuery = createSubQuery(subQuery, concept.getVar());
            subQuery.setDistinct(true);
        }

        if(itemLimit != null) {
            subQuery.setLimit(itemLimit);
        }

        Element esq = new ElementSubQuery(subQuery);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(resultVar, new ExprAggregator(concept.getVar(), new AggCount()));
        result.setQueryPattern(esq);

        return result;
    }

    /**
     *
     * @param itemLimit number of distinct resources to scan before returning a count early
     */
    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Long limit = itemLimit == null ? null : itemLimit + 1;
        Query baseQuery = concept.toQuery();
        baseQuery.setDistinct(true);
        Entry<Var, Query> e = QueryGenerationUtils.createQueryCount(baseQuery, limit, rowLimit);
        Var countVar = e.getKey();
        Query query = e.getValue();

        Single<Range<Long>> result = SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query))
            .map(b -> {
                Node countNode =  b.get(countVar);
                Number n = NodeUtils.getNumber(countNode);
                long count = n.longValue();
                boolean hasMoreItems = false;
                if(itemLimit != null && count > itemLimit) {
                    count = itemLimit;
                    hasMoreItems = true;
                }
                Range<Long> r = hasMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count);
                return r;
            })
            .singleOrError();
        // Note: Counting should always return exactly one result
        // If it doesn't it might be either to a bug in the triple store or maybe the jena
        // query doesn't have the count aggregator registered properly at the query

        return result;
    }

    @Override
    public Flowable<Entry<Node, Node>> apply(Range<Long> range) {
        Query query = concept.asQuery();
        QueryUtils.applyRange(query, range);

        //Query query = createQueryCount(concept, itemLimit, rowLimit, resultVar)
        return SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query))
                .map(b -> b.get(b.vars().next()))
                .map(node -> Maps.immutableEntry(node, node));

//    	apply(range);
//        Map<Node, Node> map = fetchMap(range);
//        return map.entrySet().stream();
    }

}
