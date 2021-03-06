package org.aksw.jena_sparql_api.conjure.entity.algebra;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
@RdfType("eg:OpCode")
public interface OpCode
	extends Op1
{
	@IriNs("eg")
	String getCoderName();
	OpCode setCoderName(String coderName);

	@IriNs("eg")
	Boolean isDecode();
	OpCode setDecode(Boolean isDecode);

	OpCode setSubOp(Op subOp);


	//List<Op> getSubOps();
	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpCode create(Op subOp, String coderName, boolean isDecode) {
		OpCode result = subOp.getModel().createResource().as(OpCode.class)
			.setSubOp(subOp)
			.setCoderName(coderName)
			.setDecode(isDecode);
		return result;
	}
}