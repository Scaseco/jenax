package org.aksw.jenax.dataaccess.sparql.exec.query;


public abstract class QueryExecFactoryQueryWrapperBase<T extends QueryExecFactoryQuery>
	implements QueryExecFactoryQuery
{
	protected T decoratee;

	public QueryExecFactoryQueryWrapperBase(T decoratee) {
		super();
		this.decoratee = decoratee;
	}
}
