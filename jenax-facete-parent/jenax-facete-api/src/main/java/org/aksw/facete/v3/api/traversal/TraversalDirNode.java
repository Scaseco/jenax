package org.aksw.facete.v3.api.traversal;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.ModelUtils;

/**
 * Interface for DirNodes
 */
public interface TraversalDirNode<N, M extends TraversalMultiNode<N>> {
	default N via(String propertyIRI, String alias) {
		return via(ResourceFactory.createProperty(propertyIRI), alias);
	}

	default N via(Node node, String alias) {
		return via(node.getURI(), alias);
	}

	default N via(String propertyIRI) {
		return via(propertyIRI, null);
	}

	default M via(Node node) {
		return via(ModelUtils.convertGraphNodeToRDFNode(node).asResource());
	}

	default N via(Resource property, String alias) {
		return via(property).viaAlias(alias);
	}

	M via(Resource property);
	
	boolean isFwd();
}
