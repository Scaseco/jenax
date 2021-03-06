package org.aksw.jenax.path.domain;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;

/**
 * Interface for DirNodes
 */
public interface TraversalGraph<N> {

    default N via(String propertyIRI) {
        return via(propertyIRI);
    }

    default N via(Node node) {
        return via(ModelUtils.convertGraphNodeToRDFNode(node).asResource());
    }

    N via(Resource property);

    boolean isFwd();
}
