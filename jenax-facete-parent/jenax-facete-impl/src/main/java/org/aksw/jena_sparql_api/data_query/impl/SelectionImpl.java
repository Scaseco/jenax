package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.jena_sparql_api.data_query.api.Selection;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;

public class SelectionImpl
	extends ResourceImpl
	implements Selection
{
    public SelectionImpl(Node n, EnhGraph m) {
        super(n, m);
    }

	@Override
	public void setAlias(String alias) {//Var alias) {
		// TODO use proper vocab
//		ResourceUtils.setLiteralProperty(this, RDFS.seeAlso, alias.getName());
		ResourceUtils.setLiteralProperty(this, RDFS.seeAlso, alias);
	}

	@Override
	public String getAlias() {
		String result = ResourceUtils.getLiteralProperty(this, RDFS.seeAlso, String.class)
		//Var result = Optional.ofNullable(getProperty(RDFS.seeAlso))
			.map(Statement::getString)
			//.map(Var::alloc)
			.orElse(null);
		
		return result;
	}
}
