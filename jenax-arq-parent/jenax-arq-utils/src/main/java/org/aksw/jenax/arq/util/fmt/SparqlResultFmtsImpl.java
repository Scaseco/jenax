package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetLang;

/**
 * Class for providing (default) mappings for the result types defined by SPARQL
 * to Lang and RDFFormat instances
 *
 * Use {@link SparqlQueryFmts} for controlling formats on the query type such that e.g.
 * describe and construct queries map to different formats.
 */
public class SparqlResultFmtsImpl implements SparqlResultFmts {
	public static final SparqlResultFmts DEFAULT = createDefault();
	public static final SparqlResultFmts XML = createXml();
	public static final SparqlResultFmts JSON = createJson();
	public static final SparqlResultFmts TXT = createTxt();

	protected Lang askResult;
	protected Lang bindings;
	protected RDFFormat triples;
	protected RDFFormat quads;
	protected Lang unknown;

	public SparqlResultFmtsImpl(Lang unknown, Lang askResult, Lang bindings, RDFFormat triples, RDFFormat quads) {
		super();
		this.unknown = unknown;
		this.askResult = askResult;
		this.bindings = bindings;
		this.triples = triples;
		this.quads = quads;
	}

	public static SparqlResultFmts createDefault() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_JSON,
				ResultSetLang.RS_JSON, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS);
	}

	public static SparqlResultFmts createJson() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_JSON,
				ResultSetLang.RS_JSON, RDFFormat.JSONLD11, RDFFormat.JSONLD11);
	}

	public static SparqlResultFmts createXml() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_XML,
				ResultSetLang.RS_XML, RDFFormat.TRIX, null);
	}

	public static SparqlResultFmts createTxt() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_Text,
				ResultSetLang.RS_Text, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS);
	}

	public static SparqlResultFmts createCsv() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_CSV,
				ResultSetLang.RS_CSV, RDFFormat.NT, RDFFormat.NQ);
	}

	public static SparqlResultFmts createTsv() {
		return new SparqlResultFmtsImpl(null, ResultSetLang.RS_TSV,
				ResultSetLang.RS_TSV, RDFFormat.NT, RDFFormat.NQ);
	}

	@Override
	public Lang forAskResult() {
		return askResult;
	}

	@Override
	public Lang forBindings() {
		return bindings;
	}

	@Override
	public RDFFormat forTriples() {
		return triples;
	}

	@Override
	public RDFFormat forQuads() {
		return quads;
	}

	@Override
	public Lang forUnknown() {
		return unknown;
	}
}
