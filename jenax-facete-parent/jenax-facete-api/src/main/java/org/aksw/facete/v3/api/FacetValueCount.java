package org.aksw.facete.v3.api;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;


/**
 * FacetValue plus a count attribute
 * 
 * @author Claus Stadler, Dec 29, 2018
 *
 */
@IriNs(Vocab.ns)
public interface FacetValueCount
	extends FacetValue
{
//	@Iri//(FacetValueCountImpl_.PREDICATE)
//	Node getPredicate();
//	
//	@Iri//(FacetValueCountImpl_.VALUE)
//	Node getValue();

	CountInfo getFocusCount();
}
