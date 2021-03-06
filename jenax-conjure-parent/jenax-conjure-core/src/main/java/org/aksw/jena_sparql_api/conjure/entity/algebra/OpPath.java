package org.aksw.jena_sparql_api.conjure.entity.algebra;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType("eg:OpPath")
public interface OpPath
	extends Op0
{
	@IriNs("eg")
	//@IriType
	String getName();
	OpPath setName(String name);	
		
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

	public static OpPath create(String name) {
		OpPath result = ModelFactory.createDefaultModel()
				.createResource().as(OpPath.class)
				.setName(name);

		return result;
	}
}