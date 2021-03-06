package org.aksw.jena_sparql_api.conjure.job.api;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@RdfTypeNs("rpif")
public interface MacroParam
	extends Resource
{
	@IriNs("rpif")
	OpVar getOpVar();
	MacroParam setOpVar(OpVar opVar);
	
//	@IriNs("rpif")
//	List<String> getParams();
//	void setParams(List<String> params);

	public static MacroParam create(Model model, OpVar opVar) {
		MacroParam result = model.createResource().as(MacroParam.class)
			.setOpVar(opVar);
			//.setVarName(varName);
	
		return result;
	}
}
