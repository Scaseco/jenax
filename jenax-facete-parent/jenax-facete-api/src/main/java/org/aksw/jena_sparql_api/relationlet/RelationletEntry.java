package org.aksw.jena_sparql_api.relationlet;

import java.util.function.Function;

import org.apache.jena.sparql.core.Var;

public class RelationletEntry<T extends Relationlet> {
	protected T relationlet;
	//protected String label; // Allow multiple labels?
	protected String id;

	public RelationletEntry(String id, T relationlet) {
		super();
		this.id = id;
		this.relationlet = relationlet;
		//this.label = label;
	}
	
	/**
	 * Create a var ref to a variable to the relationlet wrapped by this specific entry. 
	 * 
	 * @param var
	 * @return
	 */
	public VarRef createVarRef(Var var) {
		// TODO Would be nicer having a specific sub-type of VarRef for constant vars
		VarRef result = createVarRef(x -> var);
		return result;
	}
	
	public VarRefEntry createVarRef(Function<? super T, ? extends Var> varAccessor) {
		VarRefEntry result = new VarRefEntryFnImpl<T>(this, varAccessor);
		return result;
	}
	
	
	public T getRelationlet() {
		return relationlet;
	}
//	public String getLabel() {
//		return label;
//	}
	

	// Internal identifier allocated for this entry
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "" + id + ": " + relationlet;
	}
}