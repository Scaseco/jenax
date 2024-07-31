package org.aksw.jenax.graphql.sparql.v2.api2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;

/** Interface for things that can be traversed with SPARQL 1.1 property paths. */
public interface SparqlPathTraversable<T>
    extends RdfTraversable<T>
{
    @Override
    default T step(Node node, boolean isForward) {
        P_Path0 p0 = isForward ? new P_Link(node) : new P_ReverseLink(node);
        T result = step(p0);
        return result;
    }

    T step(Path path);
}
