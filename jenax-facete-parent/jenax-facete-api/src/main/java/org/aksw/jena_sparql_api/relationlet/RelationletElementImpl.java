package org.aksw.jena_sparql_api.relationlet;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletElementImpl
	extends RelationletElementBase
{
	protected Element el;
	protected Set<Var> fixedVars;

	public RelationletElementImpl(Element el) {
		this(el, new LinkedHashSet<>());
	}

	public RelationletElementImpl(Element el, Set<Var> fixedVars) {
		super();
		this.el = el;
		this.fixedVars = fixedVars;
	}

	@Override
	public Element getElement() {
		return el;
	}

	@Override
	public Set<Var> getPinnedVars() {
		return fixedVars;
	}

	@Override
	public Relationlet setPinnedVar(Var var, boolean onOrOff) {
		boolean tmp = onOrOff
			? fixedVars.add(var)
			: fixedVars.remove(var);
			
		
		return this;
	}

	@Override
	public String toString() {
		return getElement() + " (fixed " + getPinnedVars() + ")";
	}


//	@Override
//	public RelationletNested materialize() {
//		
//		return this;
//	}
}