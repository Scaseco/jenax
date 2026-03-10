package org.aksw.jenax.arq.util.syntax;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class TestQueryHash {
    @Test
    public void test01() {
        Map<String, QueryHash> hashes = new LinkedHashMap<>();
        hashes.put("h1", QueryHash.createHash("SELECT ?s COUNT(?p) FROM <http://dbpedia.org/sparql> { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2"));
        hashes.put("h2", QueryHash.createHash("SELECT COUNT(?y) ?x FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?x ?y ?z } GROUP BY ?x STR(?z) ORDER BY DESC(STR(?z)) DESC(?x) LIMIT 10 OFFSET 2"));
        hashes.put("h3", QueryHash.createHash("SELECT ?a COUNT(?b) FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?a ?b ?c } GROUP BY STR(?c) ?a ORDER BY DESC(?a) DESC(STR(?c)) LIMIT 10 OFFSET 2"));
        hashes.put("h4", QueryHash.createHash("SELECT DISTINCT ?s COUNT(?p) FROM <http://dbpedia.org/sparql> { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2"));

        hashes.put("h5", QueryHash.createHash("SELECT ?s (COUNT(?p) AS ?count) FROM <http://dbpedia.org/sparql> { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2"));
        hashes.put("h6", QueryHash.createHash("SELECT (COUNT(?y) AS ?count) ?x FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?x ?y ?z } GROUP BY ?x STR(?z) ORDER BY DESC(STR(?z)) DESC(?x) LIMIT 10 OFFSET 2"));
        hashes.put("h7", QueryHash.createHash("SELECT ?a (COUNT(?b) AS ?count) FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?a ?b ?c } GROUP BY STR(?c) ?a ORDER BY DESC(?a) DESC(STR(?c)) LIMIT 10 OFFSET 2"));

        hashes.put("h8", QueryHash.createHash("PREFIX eg: <http://www.example.org> SELECT ?a (COUNT(?b) AS ?count) FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?a ?b ?c } GROUP BY STR(?c) ?a ORDER BY DESC(?a) DESC(STR(?c)) LIMIT 10 OFFSET 2"));

        List<Entry<String, QueryHash>> entries = new ArrayList<>(hashes.entrySet());
        Assert.assertEquals(8, entries.size());

        assertPairWiseDifferent(entries, e -> e.getValue().toString());

        String expectedPrefix = "Tjny8TXJKFa7hYkh6VBRiv_6S_tZn3Fm_vyP-JtwPFM/cm60CQ/AA/MusTnQ/s/";
        for (Entry<String, QueryHash> entry : entries) {
            String hashStr = entry.getValue().toString();
            String actualPrefix = hashStr.substring(0, expectedPrefix.length());
            System.err.println(hashStr);
            Assert.assertEquals(expectedPrefix, actualPrefix);
        }
    }

    private static <I, O> void assertPairWiseDifferent(List<I> items, Function<? super I, ? extends O> fn) {
        int n = items.size();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i == j) {
                    continue;
                }
                I a = items.get(i);
                I b = items.get(j);
                O av = fn.apply(a);
                O bv = fn.apply(b);
                Assert.assertNotEquals(av, bv);
            }
        }
    }

//	public static void main(String[] args) {
//        // QueryHash h2 = QueryHash.createHash("SELECT ?s ?p { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//        // QueryHash h2 = QueryHash.createHash("SELECT ?p ?s { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//        // QueryHash h2 = QueryHash.createHash("SELECT ?p ?o { ?s ?p ?o } LIMIT 10")));
//
//        if (false) {
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT ?s COUNT(?p) FROM <http://dbpedia.org/sparql> { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2")));
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT COUNT(?y) ?x FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?x ?y ?z } GROUP BY ?x STR(?z) ORDER BY DESC(STR(?z)) DESC(?x) LIMIT 10 OFFSET 2")));
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT ?a COUNT(?b) FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?a ?b ?c } GROUP BY STR(?c) ?a ORDER BY DESC(?a) DESC(STR(?c)) LIMIT 10 OFFSET 2")));
//
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT ?s (COUNT(?p) AS ?count) FROM <http://dbpedia.org/sparql> { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2")));
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT (COUNT(?y) AS ?count) ?x FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?x ?y ?z } GROUP BY ?x STR(?z) ORDER BY DESC(STR(?z)) DESC(?x) LIMIT 10 OFFSET 2")));
//            QueryHash h2 = StringUtils.md5Hash("" + "SELECT ?a (COUNT(?b) AS ?count) FROM <http://dbpedia.org/sparql> FROM NAMED <urn:foo> { ?a ?b ?c } GROUP BY STR(?c) ?a ORDER BY DESC(?a) DESC(STR(?c)) LIMIT 10 OFFSET 2")));
//        }
//
//        // QueryHash h2 = QueryHash.createHash("SELECT (?x AS ?y) { ?s ?p ?o } LIMIT 10 OFFSET 2")));
//        // Collection<String> items = Arrays.asList("d", "c", "b", "a");
//        // BigInteger value = Lehmer.lehmerValue(items, Comparator.naturalOrder());
//    }
}
