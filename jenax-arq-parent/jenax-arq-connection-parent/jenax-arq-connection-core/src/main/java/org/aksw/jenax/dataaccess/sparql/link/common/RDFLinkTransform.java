package org.aksw.jenax.dataaccess.sparql.link.common;

import java.util.function.Function;

import org.apache.jena.rdflink.RDFLink;

public interface RDFLinkTransform
	extends Function<RDFLink, RDFLink>
{
}
