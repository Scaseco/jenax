package org.aksw.jenax.arq.connection.link;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

public class RowSetOverQueryExec
	extends RowSetDelegateBase
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
