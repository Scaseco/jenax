package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public abstract class RelationletForwarding
	implements Relationlet
{
	protected abstract Relationlet getRelationlet();
	
	@Override
	public Collection<Var> getExposedVars() {
		return getRelationlet().getExposedVars();
	}

	@Override
	public Set<Var> getVarsMentioned() {
		return getRelationlet().getVarsMentioned();
	}

	@Override
	public Set<Var> getPinnedVars() {
		return getRelationlet().getPinnedVars();
	}

	@Override
	public Relationlet setPinnedVar(Var var, boolean onOrOff) {
		return getRelationlet().setPinnedVar(var, onOrOff);
	}

	@Override
	public RelationletSimple materialize() {
		RelationletSimple result = getRelationlet().materialize();
		return result;
	}

}