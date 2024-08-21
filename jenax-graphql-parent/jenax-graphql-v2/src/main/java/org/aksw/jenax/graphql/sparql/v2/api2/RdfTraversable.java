package org.aksw.jenax.graphql.sparql.v2.api2;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

/** Interface for things that can be traversed with RDF properties in forward or backward direction. */
public interface RdfTraversable<T> {
    default T fwd(String propertyIri) {
        Node node = NodeFactory.createURI(propertyIri);
        T result = fwd(node);
        return result;
    }

    default T fwd(Node propertyNode) {
        T result = step(propertyNode, true);
        return result;
    }

    default T fwd(Resource property) {
        Node node = property.asNode();
        T result = fwd(node);
        return result;
    }

    default T bwd(String propertyIri) {
        Node node = NodeFactory.createURI(propertyIri);
        T result = bwd(node);
        return result;
    }

    default T bwd(Node propertyNode) {
        T result = step(propertyNode, false);
        return result;
    }

    default T bwd(Resource property) {
        Node node = property.asNode();
        T result = bwd(node);
        return result;
    }

    T step(Node node, boolean isForward);
}
