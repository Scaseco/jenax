package org.aksw.facete.v3.api;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.rdf.model.Property;

public interface FacetNodeResource
	extends FacetNode
{
	FacetedQueryResource query();
	
	BgpNode state();
	FacetNodeResource parent();
	
	@Override
	ConstraintFacade<? extends FacetNodeResource> constraints();

	static Property reachingProperty(FacetNode fn) {
		final FacetNodeResource fnr = fn.as(FacetNodeResource.class);
		return fnr == null ? null : fnr.state().parent().reachingProperty();
	}

	static Direction reachingDirection(FacetNode fn) {
		final FacetNodeResource fnr = fn.as(FacetNodeResource.class);
		return fnr == null ? null : fnr.state().parent().getDirection();
	}

}
