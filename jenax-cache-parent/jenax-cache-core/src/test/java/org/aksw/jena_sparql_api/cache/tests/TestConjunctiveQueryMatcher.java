package org.aksw.jena_sparql_api.cache.tests;

import java.util.Map;
import java.util.stream.Stream;

import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.jena_sparql_api.algebra.utils.ConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcher;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcherImpl;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class TestConjunctiveQueryMatcher {

    public static ConjunctiveQuery asConjunctiveQuery(Query query) {
    ConjunctiveQuery result = Stream.of(query)
        .map(Algebra::compile)
        .map(Algebra::toQuadForm)
        .map(op -> QueryToGraph.normalizeOp(op, false))
        .map(op -> (OpExtConjunctiveQuery)op)
        .map(OpExtConjunctiveQuery::getQfpc)
        .findFirst()
        .orElse(null);

        return result;
    }

    protected Query qS = QueryFactory.create("SELECT DISTINCT ?s { ?s a ?t }");
    protected ConjunctiveQuery cqS = asConjunctiveQuery(qS);

    protected Query qST = QueryFactory.create("SELECT ?s ?t { ?s a ?t }");
    protected ConjunctiveQuery cqST = asConjunctiveQuery(qST);

    @Test
    public void testConjunctiveQueryExtraction() {
        //System.out.println(cq);
        Assertions.assertNotNull(cqS);
        Assertions.assertNotNull(cqST);
        // TODO Validate correctness thoroughly

        //System.out.println(cq);
    }

    @Test
    public void testLookupWithIdenticalQuery() {
        ConjunctiveQueryMatcher<String> matcher = new ConjunctiveQueryMatcherImpl<>();
        matcher.put("test", cqS);

        ConjunctiveQuery lookupCqS = asConjunctiveQuery(QueryUtils.randomizeVars(qS));


        Map<String, QfpcMatch> map = matcher.lookup(lookupCqS);
        Assertions.assertEquals(map.size(), 1);
        System.out.println(map);
    }

    @Test
    public void testMatchingProjection() {
        ConjunctiveQueryMatcher<String> matcher = new ConjunctiveQueryMatcherImpl<>();
        matcher.put("test", cqST);

        ConjunctiveQuery lookupCqS = asConjunctiveQuery(QueryUtils.randomizeVars(qS));

        Map<String, QfpcMatch> map = matcher.lookup(lookupCqS);
        Assertions.assertEquals(map.size(), 1);
        System.out.println(map);
    }

    @Test
    public void testNonMatchingProjection() {
        ConjunctiveQueryMatcher<String> matcher = new ConjunctiveQueryMatcherImpl<>();
        matcher.put("test", cqS);

        ConjunctiveQuery lookupCqST = asConjunctiveQuery(QueryUtils.randomizeVars(qST));

        Map<String, QfpcMatch> map = matcher.lookup(lookupCqST);
        Assertions.assertEquals(map.size(), 0);
        System.out.println(map);
    }


}
