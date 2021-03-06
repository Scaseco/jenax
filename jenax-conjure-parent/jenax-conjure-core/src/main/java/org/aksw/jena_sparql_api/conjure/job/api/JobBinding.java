package org.aksw.jena_sparql_api.conjure.job.api;

import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@RdfTypeNs("rpif")
public interface JobBinding
	extends Resource
{
	@IriNs("rpif")
	String getVarName();
	JobBinding setVarName(String varName);

	@PolymorphicOnly
	@IriNs("rpif")
	OpTraversal getTraversal();
	JobBinding setTraversal(OpTraversal op);
	
	
	public static JobBinding create(Model model, String varName, OpTraversal traversal) {
		JobBinding result = model.createResource().as(JobBinding.class)
			.setTraversal(traversal)
			.setVarName(varName);
	
		return result;
	}
//	
//	public static OpConstruct create(Op subOp, Collection<String> queryStrings) {
//		OpConstruct result = subOp.getModel().createResource().as(OpConstruct.class)
//			.setSubOp(subOp)
//			.setQueryStrings(queryStrings);
//		
//		return result;
//	}

}
