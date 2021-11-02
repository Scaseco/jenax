package org.aksw.facete.v3.bgp.impl;

import java.util.Collection;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class XFacetedQueryImpl
	extends ResourceImpl
	implements XFacetedQuery
{

	public XFacetedQueryImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public Resource getBaseConcept() {
		return ResourceUtils.getPropertyValue(this, Vocab.baseConcept, Resource.class);
	}

	@Override
	public void setBaseConcept(Resource baseConcept) {
		ResourceUtils.setProperty(this, Vocab.baseConcept, baseConcept);
	}

	@Override
	public BgpNode getFocus() {
		return ResourceUtils.getPropertyValue(this, Vocab.focus, BgpNode.class);	
	}

	@Override
	public void setFocus(BgpNode focus) {
		ResourceUtils.setProperty(this, Vocab.focus, focus);	
	}

	@Override
	public BgpNode getBgpRoot() {
		return ResourceUtils.getPropertyValue(this, Vocab.root, BgpNode.class);
	}

	@Override
	public void setBgpRoot(BgpNode root) {
		ResourceUtils.setProperty(this, Vocab.root, root);
	}

	@Override
	public Collection<FacetConstraint> constraints() {
		return new SetFromPropertyValues<>(this, Vocab.constraint, FacetConstraint.class);
	}

}
