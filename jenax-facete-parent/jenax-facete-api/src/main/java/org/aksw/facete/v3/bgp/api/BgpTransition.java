package org.aksw.facete.v3.bgp.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface BgpTransition
	extends Resource
{
	Property getProperty();
	boolean isReverse();
	
	BgpNode getSource();
	BgpNode getTarget();
}
