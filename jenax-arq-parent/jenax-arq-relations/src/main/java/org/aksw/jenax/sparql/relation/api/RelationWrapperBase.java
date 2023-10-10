package org.aksw.jenax.sparql.relation.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationWrapperBase
    implements Relation
{
    protected Relation delegate;
    
    public RelationWrapperBase(Relation delegate) {
		super();
		this.delegate = delegate;
	}
    
    public Relation getDelegate() {
		return delegate;
	}

	@Override
    public Element getElement() {
        return delegate.getElement();
    }

    @Override
    public List<Var> getVars() {
        return delegate.getVars();
    }
}
