package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public interface SparqlResultFmts {
	Lang forAskResult();
	Lang forBindings();
	RDFFormat forTriples();
	RDFFormat forQuads();
	Lang forUnknown();
}