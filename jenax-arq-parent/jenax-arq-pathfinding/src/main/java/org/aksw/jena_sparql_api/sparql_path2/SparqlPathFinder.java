package org.aksw.jena_sparql_api.sparql_path2;

import java.util.List;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;


public interface SparqlPathFinder {
    List<SparqlPath> find(Node startNode, Node endNode, int k, Path path, QueryExecutionFactory qef);
}
