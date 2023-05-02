package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public class SparqlQueryFmtOverResultFmt
	implements SparqlQueryFmts
{
	protected SparqlResultFmts fmts;

	public SparqlQueryFmtOverResultFmt(SparqlResultFmts fmts) {
		super();
		this.fmts = fmts;
	}

	@Override
	public RDFFormat forDescribe() {
		return fmts.forTriples();
	}

	@Override
	public RDFFormat forConstruct() {
		return fmts.forTriples();
	}

	@Override
	public RDFFormat forConstructQuad() {
		return fmts.forQuads();
	}

	@Override
	public Lang forResultSet() {
		return fmts.forBindings();
	}

	@Override
	public Lang forAskResult() {
		return fmts.forAskResult();
	}

	@Override
	public Lang forUnknown() {
		return fmts.forUnknown();
	}
}
