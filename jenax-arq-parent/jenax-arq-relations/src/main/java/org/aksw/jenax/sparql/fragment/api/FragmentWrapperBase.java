package org.aksw.jenax.sparql.fragment.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class FragmentWrapperBase
    implements Fragment
{
    protected Fragment delegate;
    
    public FragmentWrapperBase(Fragment delegate) {
		super();
		this.delegate = delegate;
	}
    
    public Fragment getDelegate() {
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
