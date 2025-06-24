package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.dataaccess.sparql.dataset.engine.DatasetGraphOverRDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.engine.RDFLinkSourceHTTP;
import org.aksw.jenax.dataaccess.sparql.engine.RDFLinkSourceHTTPSimple;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.junit.Test;

public class TestDatasetGraphOverRDFEngine {
    @Test
    public void test() {
        RDFLinkSourceHTTP linkSource = RDFLinkSourceHTTPSimple.of(builder -> builder.destination("http://linkedgeodata.org/sparql"));
        RDFEngine engine = RDFEngines.of(linkSource);
        DatasetGraph dsg = DatasetGraphOverRDFEngine.of(engine);

        Table table = QueryExec.dataset(dsg).query("SELECT * { ?s ?p ?o } LIMIT 10").table();
        RowSetOps.out(table.toRowSet());

        Graph graph = QueryExec.dataset(dsg).query("CONSTRUCT WHERE { ?s ?p ?o } LIMIT 10").construct();
        RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_PRETTY);
    }
}
