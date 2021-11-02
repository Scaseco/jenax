package org.aksw.facete.v3.bgp.api;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;

/**
 * 
 * @author raven
 *
 */
public interface BgpDirNode extends TraversalDirNode<BgpNode, BgpMultiNode> {
	boolean isFwd();
}
