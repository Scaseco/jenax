package org.aksw.facete.v3.bgp.impl;

import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;

public class BgpDirNodeImpl
	implements BgpDirNode
{
	protected BgpNode node;
	protected boolean isFwd;
	
	public BgpDirNodeImpl(BgpNode node, boolean isFwd) {
		this.node = node;
		this.isFwd = isFwd;
	}
	
	@Override
	public boolean isFwd() {
		return isFwd;
	}
	
	/**
	 * Traverse via a given property instance with the given alias.
	 * 
	 */
	@Override
	public BgpMultiNode via(Resource property) {
		Map<Resource, BgpMultiNode> map = isFwd ? node.fwdMultiNodes() : node.bwdMultiNodes() ;
		
		BgpMultiNode result = map.get(property);
		if(result == null) {
			result = node.getModel().createResource().as(BgpMultiNode.class);
			map.put(property, result);
		}
		
		return result;
	}
}
