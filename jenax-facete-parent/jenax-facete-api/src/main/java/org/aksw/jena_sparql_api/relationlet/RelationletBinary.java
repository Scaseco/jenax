package org.aksw.jena_sparql_api.relationlet;

import java.util.Set;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletBinary
    extends RelationletElementBase
{
    protected BinaryRelation br;
    protected Set<Var> fixedVars;

    public RelationletBinary(BinaryRelation br) {
        super();
        this.br = br;
    }

    public BinaryRelation getBinaryRelation() {
        return br;
    }

    @Override
    public Element getElement() {
        return br.getElement();
    }

    @Override
    public Set<Var> getVarsMentioned() {
////		Element el = getElement();
//		Set<Var> result = super.getVarsMentioned();//ElementUtils.getVarsMentioned(el);
//		result.add(br.get)
        Set<Var> result = br.getVarsMentioned();
        return result;
    }

    @Override
    public String toString() {
        return "RelationletBinary [br=" + br + "]";
    }
//
//	@Override
//	public Var getSrcVar() {
//		return br.getSourceVar();
//	}
//
//	@Override
//	public Var getTgtVar() {
//		return br.getTargetVar();
//	}

}