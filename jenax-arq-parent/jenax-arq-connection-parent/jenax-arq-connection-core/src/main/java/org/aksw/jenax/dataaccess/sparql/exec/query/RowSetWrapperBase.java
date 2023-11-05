package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

public class RowSetWrapperBase
	implements RowSet
{
    protected RowSet delegate;

    public RowSetWrapperBase(RowSet delegate) {
        this.delegate = delegate;
    }

    protected RowSet getDelegate() {
    	return delegate;
    }

    @Override
    public List<Var> getResultVars() {
    	return getDelegate().getResultVars();
    }

    @Override
    public long getRowNumber() {
    	return getDelegate().getRowNumber();
    }

	@Override
	public boolean hasNext() {
		return getDelegate().hasNext();
	}

	@Override
	public Binding next() {
		return getDelegate().next();
	}

	@Override
	public void close() {
		getDelegate().close();
	}

}
