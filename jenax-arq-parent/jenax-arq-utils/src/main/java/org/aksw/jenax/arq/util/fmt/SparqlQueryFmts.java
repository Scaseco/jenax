package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/**
 * Interface which captures which serialization format to use for which query type.
 */
public interface SparqlQueryFmts {
	RDFFormat forDescribe();
	RDFFormat forConstruct();
	RDFFormat forConstructQuad();
	Lang forResultSet();
	Lang forAskResult();

	Lang forUnknown();
}
