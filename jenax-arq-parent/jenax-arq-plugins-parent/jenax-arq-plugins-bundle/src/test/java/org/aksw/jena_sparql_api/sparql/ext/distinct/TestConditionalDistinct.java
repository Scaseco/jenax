package org.aksw.jena_sparql_api.sparql.ext.distinct;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.junit.Ignore;
import org.junit.Test;

public class TestConditionalDistinct {
    @Test
    @Ignore // FIXME Bug in jena-serviceenhancer: It incorrectly removes the colon.
    public void test01() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        for (int gi = 0; gi < 10; ++gi) { // All graphs have the same data
            Node g = NodeFactory.createURI("urn:g" + gi);
            for (int si = 0; si < 1; ++si) {
                Node s = NodeFactory.createURI("urn:s" + si);
                for (int pi = 0; pi < 2; ++pi) {
                    Node p = NodeFactory.createURI("urn:p" + pi);
                    for (int oi = 0; oi < 2; ++oi) {
                        Node o = NodeFactory.createURI("urn:o" + oi);
                        dsg.add(Quad.create(g, s, p, o));
                    }
                }
            }
        }
        RDFDataMgr.write(System.out, DatasetFactory.wrap(dsg), RDFFormat.TRIG_PRETTY);

        String queryStr = String.join("\n",
            "SELECT * {",
            "  SERVICE <distinct:> {",
            "    SERVICE <if:> { FILTER (?p = <urn:p1>) }",
            "    SERVICE <if:> { FILTER (?p = <urn:p2>) }",
            "    SERVICE <over:> { SELECT ?s ?p ?o { GRAPH ?g { ?s ?p ?o } } }",
            "  }",
            "}"
        );
        System.out.println(queryStr);

        try(QueryExec qe = QueryExec.newBuilder()
            .dataset(dsg)
            .query(queryStr)
            .build()) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
        }
    }
}
