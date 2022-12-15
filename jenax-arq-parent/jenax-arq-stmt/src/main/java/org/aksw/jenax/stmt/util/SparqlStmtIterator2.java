package org.aksw.jenax.stmt.util;

// Looking into improving SparqlStmtIterator based on insights into tarql code
// However, while tarql code shows how to parse either multiple queries OR multiple update statements from a stream,
// it seems that the approach cannot be adapted to parse a mix of update / query statements
//public class SparqlStmtIterator2
//    extends AbstractIterator<SparqlStmt>
//{
//    protected SPARQLParser parser;
//
//	@Override
//	protected SparqlStmt computeNext() {
//		ARQParser x;
//		QueryFactory.create(null)
//		SPARQLParser.createParser(syntaxURI)
//		x.getNextToken();
//
//
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//	private void parseDo(SPARQLParser11 parser) throws ParseException {
//		do {
//			int beginLine = parser.getToken(1).beginLine;
//			int beginColumn = parser.getToken(1).beginColumn;
//
//			Query query = new Query(result.getPrologue());
//
//			// You'd assume that a query initialized via "new Query(prologue)"
//			// has the IRI resolver from prologue.getResolver(), but that doesn't
//			// appear to be the case in Jena 2.12.0, so we set it manually
//			query.getPrologue().setResolver(result.getPrologue().getResolver());
//
//			result.addQuery(query);
//			parser.setQuery(query);
//			parser.Query();
//
//			if (query.isSelectType() || query.isAskType()) {
//				seenSelectOrAsk = true;
//			}
//			if (seenSelectOrAsk && result.getQueries().size() > 1) {
//				throw new QueryParseException("" +
//						"Multiple queries per file are only supported for CONSTRUCT",
//						beginLine, beginColumn);
//			}
//
//			// From Parser.validateParsedQuery, which we can't call directly
//			SyntaxVarScope.check(query);
//
//			result.getPrologue().usePrologueFrom(query);
//			if (log.isDebugEnabled()) {
//				log.debug(query.toString());
//			}
//		} while (parser.getToken(1).kind != SPARQLParser11.EOF);
//		removeBuiltInPrefixes();
//	}
//
//	// Adapted from ARQ ParserSPARQL11.java
//	private void parse() {
//		if (done) return;
//		done = true;
//		SPARQLParser11 parser = new SPARQLParser11(reader) ;
//		try {
//			parseDo(parser);
//		} catch (ParseException ex) {
//			throw new QueryParseException(ex.getMessage(),
//					ex.currentToken.beginLine,
//					ex.currentToken.beginColumn);
//		} catch (TokenMgrError tErr) {
//			// Last valid token : not the same as token error message - but this should not happen
//			int col = parser.token.endColumn;
//			int line = parser.token.endLine;
//			throw new QueryParseException(tErr.getMessage(), line, col);
//		} catch (QueryException ex) {
//			throw ex;
//		} catch (JenaException ex) {
//			throw new QueryException(ex.getMessage(), ex);
//		} catch (Error err) {
//			// The token stream can throw errors.
//			throw new QueryParseException(err.getMessage(), err, -1, -1);
//		} catch (Throwable th) {
//			Log.warn(TarqlParser.class, "Unexpected throwable: ",th);
//			throw new QueryException(th.getMessage(), th);
//		}
//	}
//
//}
