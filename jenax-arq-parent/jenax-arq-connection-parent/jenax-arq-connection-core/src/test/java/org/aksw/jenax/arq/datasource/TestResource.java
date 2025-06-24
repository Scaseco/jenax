package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class TestResource {
    @Test
    public void test() {
        Dataset ds = DatasetFactory.create();
        ds.getDefaultModel().getGraph()
            .add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property);

        RDFDataSource dataSource = RDFDataSources.of(ds);
        testResource(dataSource);
    }

    public static void testResource(RDFDataSource ds) {
        try (RDFConnection conn = ds.getConnection()) {
            try (QueryExecution qe = conn.query("SELECT ?s { ?s a ?o }")) {
                ResultSet rs = qe.execSelect();
                QuerySolution qs = rs.next();
                RDFNode sNode = qs.get("s");
                Resource s = sNode.asResource();
                Statement stmt = s.getProperty(RDF.type);
                RDFNode o = stmt.getObject();
                Assert.assertEquals(RDF.Property, o);
            }
        }
    }
}
