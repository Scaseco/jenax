package org.aksw.jena_sparql_api.data_query.api;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class QuerySpecImpl
    implements QuerySpec
{
    protected Query query; // a sparql construct query
    protected Node rootNode; // root node in the construct query's template
    protected List<Var> primaryKeyVars; // vars of the rootNode that make up an object's identity

    public QuerySpecImpl(Query query, Node rootNode, List<Var> primaryKeyVars) {
        super();
        this.query = query;
        this.rootNode = rootNode;
        this.primaryKeyVars = primaryKeyVars;
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public List<Var> getPrimaryKeyVars() {
        return primaryKeyVars;
    }

    @Override
    public String toString() {
        return "QuerySpecImpl [query=" + query + ", rootNode=" + rootNode + ", primaryKeyVars=" + primaryKeyVars + "]";
    }
}
