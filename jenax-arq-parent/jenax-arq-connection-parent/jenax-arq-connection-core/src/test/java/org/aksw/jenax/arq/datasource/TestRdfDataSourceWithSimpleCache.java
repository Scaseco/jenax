package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceTransforms;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithSimpleCache;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.junit.Assert;
import org.junit.Test;

public class TestRdfDataSourceWithSimpleCache {
    @Test
    public void testErrorCache01() {
        RDFDataSource base = RDFDataSources.alwaysFail();
        // RdfDataSource base = () -> RDFConnectionRemote.newBuilder().destination("http://dbpedia.org/sparql").build();

        RdfDataSourceWithSimpleCache ds = (RdfDataSourceWithSimpleCache)RDFDataSources.decorate(base, RdfDataSourceTransforms.simpleCache());

        String queryString = "FOO";
        // String queryString = "SELECT (COUNT(*) AS ?c) { ?s a <http://dbpedia.org/ontology/Person> }";

        Exception a = null, b = null;
        int c1 = -1, c2 = -1;
        try {
            try (QueryExecution qe1 = ds.asQef().createQueryExecution(queryString)) {
                c1 = ResultSetFormatter.consume(qe1.execSelect());
            }
        } catch (Exception e1) {
            a = e1;
        }

        try (QueryExecution qe2 = ds.asQef().createQueryExecution(queryString)) {
            c2 = ResultSetFormatter.consume(qe2.execSelect());
        } catch (Exception e2) {
            b = e2;
        }

        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertSame(a, b);

//        System.err.println(ds.getCache().stats());
//        System.err.println(c1 + " - " + c2);
    }
}
