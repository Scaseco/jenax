package org.aksw.jenax.arq.connection.link;

import org.apache.jena.rdflink.LinkSparqlQuery;

/** Central utility class for working with QueryExecFactory* classes */
public class QueryExecFactories {

	/** {@link LinkSparqlQuery} -&gt; {@link QueryExecFactoryQuery} */
	public static QueryExecFactoryQuery adapt(LinkSparqlQuery link) {
		return new QueryExecFactoryQueryOverLinkSparqlQuery(link);
	}

	public static LinkSparqlQuery toLink(QueryExecFactoryQuery qef) {
		return new LinkSparqlQueryJenaxBase<QueryExecFactoryQuery>(qef);
	}
}
