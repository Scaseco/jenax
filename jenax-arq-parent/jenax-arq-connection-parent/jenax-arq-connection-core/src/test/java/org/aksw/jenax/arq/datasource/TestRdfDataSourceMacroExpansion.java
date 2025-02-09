package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.arq.util.triple.SetFromGraph;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
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
    @Test
    public void testUpdate() {
        Map<String, UserDefinedFunctionDefinition> udfRegistry = RdfDataSources.loadMacros("datasource-test-macros.ttl");

        Graph graph = GraphFactory.createDefaultGraph();
        RdfDataSource dataSource = RdfDataSources.of(graph);
        dataSource = RdfDataSources.wrapWithMacros(dataSource, udfRegistry);

        Table expectedTable = SSE.parseTable("(table (row (?x 'Hello Anne!')))");
        Table actualTable = dataSource.asLinkSource()
            .newQuery()
            .query("PREFIX eg: <http://www.example.org/> SELECT (eg:greet('Anne') AS ?x) { }")
            .table();
        Assert.assertEquals(expectedTable, actualTable);

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
