package org.aksw.jenax.arq.datasource;

import java.util.concurrent.Callable;

import org.aksw.commons.util.benchmark.BenchmarkUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.junit.Test;

public class TestRdfDataSourceWithLocalCache {

    @Test // TODO Add a 'delaying' data source to simulate latency
    public void test() {
        RdfDataSource base = () -> RDFConnectionRemote.newBuilder()
                //.destination("https://query.wikidata.org/sparql")
                //.destination("https://dbpedia.org/sparql")
                .destination("https://coypu.demo.aksw.org/ds")
                .build();

        RdfDataSource ds = new RdfDataSourceWithLocalCache(base);

        Callable<?> action = () -> {
            try (RDFConnection conn = ds.getConnection()) {
                try (QueryExecution qe = conn.query("SELECT (COUNT(*) AS ?c) { SELECT * { ?s ?p ?o } LIMIT 5000000 }")) {
                    ResultSetFormatter.out(System.out, qe.execSelect());
                }
                return null;
            }
        };

        System.out.println("Action 1: " + BenchmarkUtils.opsPerSecByIterations(1, action));
        System.out.println("Action 2: " + BenchmarkUtils.opsPerSecByIterations(1, action));
    }
}
