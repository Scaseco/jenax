package org.aksw.facete.v3.api.path;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.apache.jena.sparql.core.Var;

public interface NestedVarMap
//	extends Cloneable
{
	
	Set<Var> getVarsMentioned();
	NestedVarMap get(String alias);
	void transformValues(Function<? super Var, ? extends Var> fn);

	Set<Var> getFixedFinalVars();
	Map<Var, Var> getLocalToFinalVarMap();
	Map<String, NestedVarMap> getMemberVarMap();

	default NestedVarMap get(String ... aliases) {
		NestedVarMap result = get(Arrays.asList(aliases));
		return result;
	}

	default NestedVarMap get(List<String> aliases) {
		NestedVarMap result;
		if(aliases.isEmpty()) {
			result = this;
		} else {
			String alias = aliases.iterator().next();
			
			List<String> sublist = aliases.subList(1, aliases.size());
			result = get(alias).get(sublist);
		}
		
		return result;
	}
	
	default boolean isFixed(Var var) {
		Set<Var> fixedVars = getFixedFinalVars();
		boolean result = fixedVars.contains(var);
		return result;
	}
	
	default boolean isFixed(VarRefStatic varRef) {
		List<String> labels = varRef.getLabels();
		NestedVarMap nvm = get(labels);
		Var v = varRef.getV();
		boolean result = nvm.isFixed(v);
		return result;
	}
	
	NestedVarMap clone();// throws CloneNotSupportedException;
}