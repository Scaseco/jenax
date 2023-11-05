package org.aksw.facete.v3.api.traversal;

import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.ModelUtils;

/**
 * Interface for DirNodes
 */
public interface TraversalDirNode<N, M extends TraversalMultiNode<N>> {
    default N via(String propertyIRI, String alias) {
        return via(ResourceFactory.createProperty(propertyIRI), null, alias);
    }

    default N via(Node node, String alias) {
        return via(node.getURI(), null, alias);
    }

    default N via(String propertyIRI) {
        return via(propertyIRI, null, null);
    }

    default M via(Node node) {
        return via(ModelUtils.convertGraphNodeToRDFNode(node).asResource());
    }

    default N via(Resource property, String alias) {
        return via(property, (Node)null).viaAlias(alias);
    }

    default M via(Resource property) {
        return via(property, FacetStep.TARGET);
    }

    M via(Resource property, Node component);


    boolean isFwd();


    default N via(String propertyIRI, Node component, String alias) {
        return via(ResourceFactory.createProperty(propertyIRI), component, alias);
    }

    default N via(Node node, Node component, String alias) {
        return via(node.getURI(), component, alias);
    }

    default N via(String propertyIRI, Node component) {
        return via(propertyIRI, component, null);
    }

    default M via(Node node, Node component) {
        return via(ModelUtils.convertGraphNodeToRDFNode(node).asResource(), component);
    }

    default N via(Resource property, Node component, String alias) {
        return via(property, component).viaAlias(alias);
    }
}
