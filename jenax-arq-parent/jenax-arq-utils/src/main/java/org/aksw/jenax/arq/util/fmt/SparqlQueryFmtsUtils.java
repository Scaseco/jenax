package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;

public class SparqlQueryFmtsUtils {

	/** Derive a Lang for serializing the query's results w.r.t. the given format configuration */
	public static Lang getLang(SparqlQueryFmts fmts, Query query) {
		Lang result;
		switch (query.queryType()) {
		case ASK:
			result = fmts.forAskResult();
			break;
		case SELECT:
			result = fmts.forResultSet();
			break;
		case CONSTRUCT:
			result = query.isConstructQuad()
				? fmts.forConstructQuad().getLang()
				: fmts.forConstruct().getLang();
			break;
		case DESCRIBE:
			result = fmts.forDescribe().getLang();
			break;
		default:
			result = fmts.forUnknown();
			break;
		}
		return result;
	}
}
