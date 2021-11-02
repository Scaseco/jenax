package org.aksw.facete.v3.impl;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class RangeSpecImpl
	extends ResourceImpl
	implements RangeSpec
{
	public RangeSpecImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public RDFNode getMin() {
		return ResourceUtils.getPropertyValue(this, Vocab.min, RDFNode.class);
	}

	@Override
	public void setMin(RDFNode min) {
		ResourceUtils.setProperty(this, Vocab.min, min);
	}

	@Override
	public boolean isMinInclusive() {
		return ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.minInclusive, Boolean.class).orElse(true);
	}

	@Override
	public void setMinInclusive(boolean onOrOff) {
		 ResourceUtils.setLiteralProperty(this, Vocab.minInclusive, onOrOff ? true : null);
	}

	@Override
	public RDFNode getMax() {
		return ResourceUtils.getPropertyValue(this, Vocab.max, RDFNode.class);
	}

	@Override
	public void setMax(RDFNode max) {
		ResourceUtils.setProperty(this, Vocab.max, max);
	}

	@Override
	public boolean isMaxInclusive() {
		return ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.maxInclusive, Boolean.class).orElse(true);
	}

	@Override
	public void setMaxInclusive(boolean onOrOff) {
		 ResourceUtils.setLiteralProperty(this, Vocab.maxInclusive, onOrOff ? true : null);
	}
}
