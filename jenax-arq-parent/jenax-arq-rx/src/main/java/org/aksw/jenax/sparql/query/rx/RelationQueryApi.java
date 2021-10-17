package org.aksw.jenax.sparql.query.rx;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Single;

public class RelationQueryApi {

    public static Single<Range<Long>> fetchCountConcept(SparqlQueryConnection conn, UnaryRelation concept, Long itemLimit, Long rowLimit) {
        return fetchCountConcept(conn::query, concept, itemLimit, rowLimit);
    }


    public static Single<Range<Long>> fetchCountConcept(Function<? super Query, ? extends QueryExecution> qef, UnaryRelation concept, Long itemLimit, Long rowLimit) {

        Var outputVar = ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Query countQuery = ConceptUtils.createQueryCount(concept, outputVar, xitemLimit, xrowLimit);

        return SparqlRx.fetchNumber(qef, countQuery, outputVar)
                .map(count -> SparqlRx.toRange(count.longValue(), xitemLimit, xrowLimit));
    }
}
