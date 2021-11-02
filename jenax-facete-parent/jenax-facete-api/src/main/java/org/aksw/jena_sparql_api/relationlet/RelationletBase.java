package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;

public abstract class RelationletBase
	implements Relationlet
{
	@Override
	public Relationlet setPinnedVar(Var var, boolean onOrOff) {
		Collection<Var> fixedVars = getPinnedVars();
		if(onOrOff) {
			fixedVars.add(var);
		} else {
			fixedVars.remove(var);
		}

		return this;
	}
}
