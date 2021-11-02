package org.aksw.jena_sparql_api.data_query.api;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public interface QuerySpec {
    Query getQuery();
    Node getRootNode();
    List<Var> getPrimaryKeyVars();
}
