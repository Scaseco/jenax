package org.aksw.jena_sparql_api.conjure.test;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.junit.Assert;
import org.junit.Test;


public class TestConjureQueryOverViews {
    // FIXME This test broke with Jena 4.3.0 because of a NPE - Investigate!
    // @Test
    public void testViews() throws Exception {
        Model expected = RDFDataMgr.loadModel("expected.ttl");

        List<Query> queries = SparqlStmtMgr.loadQueries("views.sparql", new PrefixMapping2(PrefixMapping.Extended));

        Op op = ConjureBuilderImpl.start()
            .fromDataRefFn(model -> RdfDataRefUrl.create(model, "test-data.ttl"))
            .views(queries)
            .getOp();

        try(RdfDataPod pod = ExecutionUtils.executeJob(op)) {
            try(RDFConnection conn = pod.getConnection()) {
                Model actual = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
//				RDFDataMgr.write(System.out, actual, RDFFormat.TURTLE_PRETTY);
                boolean isIsomorphic = actual.isIsomorphicWith(expected);
                Assert.assertTrue(isIsomorphic);
            }
        }
    }
}
