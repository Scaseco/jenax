package org.aksw.facete.v3.api;

import org.aksw.facete.v3.bgp.api.XFacetedQuery;

public interface FacetedQueryResource
	extends FacetedQuery//, Resource
{
	FacetNodeResource root();
	
	//FacetedQueryResource root(Resource newRoot);
	
	FacetNodeResource focus();

	XFacetedQuery modelRoot();
}
