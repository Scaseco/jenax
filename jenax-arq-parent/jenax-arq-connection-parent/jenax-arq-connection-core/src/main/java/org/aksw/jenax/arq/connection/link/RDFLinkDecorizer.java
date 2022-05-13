package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.rdflink.RDFLink;

public interface RDFLinkDecorizer
	extends Function<RDFLink, RDFLink>
{
}
