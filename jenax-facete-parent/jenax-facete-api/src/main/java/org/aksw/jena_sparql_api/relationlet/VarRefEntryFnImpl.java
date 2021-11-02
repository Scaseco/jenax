package org.aksw.jena_sparql_api.relationlet;

import java.util.function.Function;

import org.apache.jena.sparql.core.Var;

public class VarRefEntryFnImpl<T extends Relationlet>
	implements VarRefEntry
{
	protected RelationletEntry<T> entry;
	protected Function<? super T, ? extends Var> varFn;
	
	public VarRefEntryFnImpl(RelationletEntry<T> entry, Function<? super T, ? extends Var> varFn) {
		super();
		this.entry = entry;
		this.varFn = varFn;
	}

	@Override
	public RelationletEntry<?> getEntry() {
		return entry;
	}

	@Override
	public Var getVar() {
		T relationlet = entry.getRelationlet();
		Var result = varFn.apply(relationlet);
		return result;
	}

	@Override
	public String toString() {
		return "" + entry.getId() + "." + varFn.apply(entry.getRelationlet());
	}
}