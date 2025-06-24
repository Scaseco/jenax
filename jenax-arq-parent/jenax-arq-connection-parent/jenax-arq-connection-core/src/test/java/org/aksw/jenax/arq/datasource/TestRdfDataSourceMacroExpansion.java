package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.arq.util.triple.SetFromGraph;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSources;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestRdfDataSourceMacroExpansion {

    public static RDFDataSource dataSourceWithMacros(Graph graph) {
        Map<String, UserDefinedFunctionDefinition> udfRegistry = RDFLinkSources.loadMacros("datasource-test-macros.ttl");
        RDFDataSource dataSource = RDFDataSources.of(graph);
        return RDFDataSources.wrapWithMacros(dataSource, udfRegistry);
    }

    @Test
    public void testMacrosInQuery() {
        Graph graph = GraphFactory.createDefaultGraph();
        RDFDataSource dataSource = dataSourceWithMacros(graph);

        Table expectedTable = SSE.parseTable("(table (row (?x 'Hello Anne!')))");
        Table actualTable = dataSource.asLinkSource()
            .newQuery()
            .query("PREFIX eg: <http://www.example.org/> SELECT (eg:greet('Anne') AS ?x) { }")
            .table();
        Assert.assertEquals(expectedTable, actualTable);
    }

    public void testMacrosInUpdate() {
        Graph graph = GraphFactory.createDefaultGraph();
        RDFDataSource dataSource = dataSourceWithMacros(graph);

        dataSource.newUpdate().update("""
            PREFIX eg: <http://www.example.org/>
              INSERT { eg:s eg:p ?x }
              WHERE  { BIND(eg:greet('Anne') AS ?x) }
            """)
            .build()
            .execute();

        Graph expectedGraph = SSE.parseGraph("(graph (eg:s eg:p 'Hello Anne!'))", PrefixMapping.Extended);
        Set<Triple> expectedSet = SetFromGraph.wrap(expectedGraph);
        Set<Triple> actualSet = SetFromGraph.wrap(graph);
        Assert.assertEquals(expectedSet, actualSet);
    }
}
