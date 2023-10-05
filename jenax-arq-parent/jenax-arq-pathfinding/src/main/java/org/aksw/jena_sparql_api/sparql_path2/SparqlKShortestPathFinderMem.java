package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.util.Directed;
import org.aksw.commons.util.triplet.TripletPath;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class SparqlKShortestPathFinderMem
    implements SparqlKShortestPathFinder
{
    protected QueryExecutionFactoryQuery qef;

    public SparqlKShortestPathFinderMem(QueryExecutionFactoryQuery qef) {
        this.qef = qef;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start, Node end, Path path, Long k) {

        final List<NestedPath<Node, Node>> rdfPaths = new ArrayList<>();

        PathExecutionUtils.executePath(path, start, end, qef, p -> {
            rdfPaths.add(p);
            boolean r = k == null ? false : rdfPaths.size() >= k;
            return r; });


        Iterator<TripletPath<Node, Directed<Node>>> result = rdfPaths.stream()
        	.map(NestedPath::asSimpleDirectedPath)
        	.iterator();
//        Iterator<NestedPath<Node, Node>> result = rdfPaths.iterator();
//        SparqlKShortestPathFinderYen.convertPath(path)
        return result;
//        return null;
    }
}
