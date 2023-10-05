package org.aksw.jenax.arq.datasource;

import java.util.concurrent.Callable;

import org.aksw.commons.util.benchmark.BenchmarkUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithLocalCache;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.junit.Test;

public class TestRdfDataSourceWithLocalCache {

    // @Test // TODO Add a 'delaying' data source to simulate latency
    public void test() {
        RdfDataSource base = () -> RDFConnectionRemote.newBuilder()
                //.destination("https://query.wikidata.org/sparql")
                //.destination("https://dbpedia.org/sparql")
                //.destination("https://coypu.demo.aksw.org/ds")
                .destination("http://localhost:8642/sparql")
                .build();

        RdfDataSource ds = new RdfDataSourceWithLocalCache(base);

        // String str = "SELECT (COUNT(*) AS ?c) { SELECT * { ?s ?p ?o } LIMIT 5000000 }";
        String queryStr = "SELECT  DISTINCT ?v_1\n"
                + "WHERE\n"
                + "  { ?v_1  ?p  ?o\n"
                + "    FILTER ( regex(str(?v_1), \"h\", \"i\") || EXISTS { ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  ?v_2\n"
                + "                                                    FILTER regex(str(?v_2), \"h\", \"i\")\n"
                + "                                                  } )\n"
                + "  }\n"
                + "";
        Callable<?> action = () -> {
            try (RDFConnection conn = ds.getConnection()) {
                try (QueryExecution qe = conn.query(queryStr)) {
                    ResultSetFormatter.out(System.out, qe.execSelect());
                }
                return null;
            }
        };

        System.out.println("Action 1: " + BenchmarkUtils.opsPerSecByIterations(1, action));
        System.out.println("Action 2: " + BenchmarkUtils.opsPerSecByIterations(1, action));
    }
}
