package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.apache.jena.sparql.core.Var;

// Probably we need to distinguish between simple relationlets with 'constant' vars
// and those with dynamic vars, which means, that variable referred to by a varref can change 
public abstract class RelationletBaseWithMutableFixedVars
	extends RelationletBase
{
	protected Set<Var> fixedVars = new LinkedHashSet<>();
	protected Set<Var> exposedVars = new LinkedHashSet<>();

	@Override
	public Collection<Var> getExposedVars() {
		return exposedVars;
	}
	
	@Override
	public Set<Var> getPinnedVars() {
		return fixedVars;
	}


	@Override
	public NestedVarMap getNestedVarMap() {
		return null;
	}	
}