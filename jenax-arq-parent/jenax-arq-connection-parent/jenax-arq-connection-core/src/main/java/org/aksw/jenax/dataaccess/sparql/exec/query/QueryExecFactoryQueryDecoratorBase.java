package org.aksw.jenax.dataaccess.sparql.exec.query;


public abstract class QueryExecFactoryQueryDecoratorBase<T extends QueryExecFactoryQuery>
	implements QueryExecFactoryQuery
{
	protected T decoratee;

	public QueryExecFactoryQueryDecoratorBase(T decoratee) {
		super();
		this.decoratee = decoratee;
	}
}
