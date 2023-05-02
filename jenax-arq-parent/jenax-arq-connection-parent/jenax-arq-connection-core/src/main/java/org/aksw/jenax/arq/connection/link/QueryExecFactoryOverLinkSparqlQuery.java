package org.aksw.jenax.arq.connection.link;

import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecFactoryOverLinkSparqlQuery
	implements QueryExecFactory
{
	protected LinkSparqlQuery link;

	public QueryExecFactoryOverLinkSparqlQuery(LinkSparqlQuery link) {
		super();
		this.link = link;
	}

	@Override
	public QueryExec create(Query query) {
		QueryExec result = link.query(query);
		return result;
	}

	@Override
	public QueryExec create(String queryString) {
		QueryExec result = link.query(queryString);
		return result;
	}

	@Override
	public void close() throws Exception {
		link.close();
	}
}
