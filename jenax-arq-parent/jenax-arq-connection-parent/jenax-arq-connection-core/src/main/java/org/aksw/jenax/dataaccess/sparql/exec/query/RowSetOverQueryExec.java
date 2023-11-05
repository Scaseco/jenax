package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

public class RowSetOverQueryExec
	extends RowSetWrapperBase
{
	protected QueryExec queryExec;

	public RowSetOverQueryExec(QueryExec queryExec) {
		super(null);
		this.queryExec = queryExec;
	}

	@Override
	protected RowSet getDelegate() {
		if (delegate == null) {
			delegate = queryExec.select();
		}
		return delegate;
	}

	@Override
	public void close() {
		if (delegate != null) {
			delegate.close();
		}
	}
}
