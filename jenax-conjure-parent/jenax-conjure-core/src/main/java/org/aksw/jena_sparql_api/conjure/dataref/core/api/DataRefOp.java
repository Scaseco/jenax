package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefOp
	extends DataRef
{
	Object getOp();

	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

}
