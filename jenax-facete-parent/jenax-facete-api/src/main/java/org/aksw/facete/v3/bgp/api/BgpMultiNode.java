package org.aksw.facete.v3.bgp.api;

import java.util.Collection;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface BgpMultiNode
	extends Resource, TraversalMultiNode<BgpNode> {
	BgpNode parent();
	
	Property reachingProperty();
	
	Direction getDirection();
	
	/**
	 * getOrCreate the one single alias - and marks it as the default -
	 * for this multi node. Raises an exception if multiple default aliases exist
	 */
	BgpNode one();
	boolean contains(BgpNode bgpNode);
	
	Collection<BgpNode> children();
}
