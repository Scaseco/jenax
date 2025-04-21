package org.aksw.jenax.graphql.sparql.test2;

import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

public class TestOpDatasetNames {
    public static void main(String[] args) {
        Query before = QueryFactory.create("SELECT * { GRAPH ?g { } }");
        Query after = QueryTransformOps.transform(before, new ElementTransformSubst(
                Map.of(Var.alloc("g"), Var.alloc("x"))));
        System.out.println(after);
    }

    public static void main2(String[] args) {
        DatasetGraph dsg = SSE.parseDatasetGraph("""
        (dataset
          (graph :x (:x :x :x))
          (graph :y (:x :x :x))
        )
        """);

        // Works here - but breaks on copper/coypu server
        // Could be bug in an older jena version or in one of the plugins!
        // (e.g. geosparql / text / reasoning)
        String query = """
        SELECT * {
          BIND('foo' AS ?foo)
          GRAPH ?g { }
        }
        """;

        try (QueryExec qe = QueryExec.dataset(dsg).query(query).build()) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
        }
    }
}
