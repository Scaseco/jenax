package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.aksw.facete.v3.api.path.NestedVarMapImpl;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletNestedImpl
    extends RelationletBase
    implements RelationletSimple
{
    protected Element el;
    protected NestedVarMap varMap;
    protected Map<String, RelationletSimple> aliasToMember;
//	protected Map<Var, Var> exposedVarToElementVar;

    public RelationletNestedImpl(
            Element el,
            Map<Var, Var> varMap,
            Set<Var> fixedVars) {
        this(el, new NestedVarMapImpl(varMap, fixedVars), Collections.emptyMap());
    }

    public RelationletNestedImpl(
            Element el,
            NestedVarMap varMap,
            Map<String, RelationletSimple> aliasToMember) {
        super(); //el);
        this.el = el;
        this.varMap = varMap;
        this.aliasToMember = aliasToMember;
//		this.aliasToMember = aliasToMember;
//		this.exposedVarToElementVar = exposedVarToElementVar;
    }

    //@Override
    public Set<Var> getVarsMentionedCore() {
        Element el = getElement();
        Set<Var> result = ElementUtils.getVarsMentioned(el);
        return result;
    }

    @Override
    public Set<Var> getVarsMentioned() {
        Set<Var> result = new HashSet<>(getVarsMentionedCore());
        Set<Var> mappedVars = varMap.getVarsMentioned();
        result.addAll(mappedVars);

        return result;
    }

    @Override
    public NestedVarMap getNestedVarMap() {
        return varMap;
    }

//
//	@Override
//	public RelationletNested getMember(String alias) {
//		return null;
//		//return aliasToMember.get(alias);
//	}
//
//	@Override
//	public Var getInternalVar(Var var) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<Var> getExposedVars() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Set<Var> getVarsMentioned() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
    @Override
    public Set<Var> getPinnedVars() {
        return varMap.getFixedFinalVars();
    }
//
    @Override
    public Relationlet setPinnedVar(Var var, boolean onOrOff) {
        throw new UnsupportedOperationException("Cannot mark vars as fixed on this object");
    }

//	@Override
//	public Relationlet getMember(String alias) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Var getInternalVar(Var var) {
//		// TODO Auto-generated method stub
//		return null;
//	}

    @Override
    public Collection<Var> getExposedVars() {
        return null;
    }

    @Override
    public RelationletSimple materialize() {
        return this;
    }

    @Override
    public Element getElement() {
        return el;
    }
}