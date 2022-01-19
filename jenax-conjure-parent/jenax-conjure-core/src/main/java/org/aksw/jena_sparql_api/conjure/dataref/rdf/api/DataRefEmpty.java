package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefEmpty
	extends PlainDataRefUrl, DataRef
{
	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefEmpty create() {
		DataRefEmpty result = ModelFactory.createDefaultModel().createResource().as(DataRefEmpty.class);
		return result;
	}
}
