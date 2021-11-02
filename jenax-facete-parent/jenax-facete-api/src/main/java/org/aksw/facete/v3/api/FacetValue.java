package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

@IriNs(Vocab.ns)
public interface FacetValue
	extends Resource
{
	@Iri//(FacetValueCountImpl_.PREDICATE)
	Node getPredicate();
	
	@Iri//(FacetValueCountImpl_.VALUE)
	Node getValue();
}
