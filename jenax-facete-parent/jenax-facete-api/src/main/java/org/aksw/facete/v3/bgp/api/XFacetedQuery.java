package org.aksw.facete.v3.bgp.api;

import java.util.Collection;

import org.aksw.facete.v3.api.FacetConstraint;
import org.apache.jena.rdf.model.Resource;

public interface XFacetedQuery
	extends Resource
{
	Resource getBaseConcept();
	void setBaseConcept(Resource baseConcept);
	
	BgpNode getFocus();
	void setFocus(BgpNode focus);
	
	BgpNode getBgpRoot();
	void setBgpRoot(BgpNode root);
	
	Collection<FacetConstraint> constraints();
}
