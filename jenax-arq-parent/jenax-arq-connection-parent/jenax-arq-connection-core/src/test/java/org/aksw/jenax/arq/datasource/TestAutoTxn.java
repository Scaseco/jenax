package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Test;

public class TestAutoTxn {
    @Test
    public void test() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        RDFEngine engine = RDFEngines.of(dsg);

        engine = RDFEngines.decorate(engine)
            .decorate(RDFLinkTransforms.withAutoTxn())
            // .decorate(RDFLinkTransforms.withWorkerThread())
            .build();

        engine.getLinkSource().newLink().newQuery()
            .query("SELECT (COUNT(*) AS ?c) { ?s ?p ?o }").table();

        // XXX If the execution worked its already good - but should verify that autoTxn has actually been applied
    }
}
