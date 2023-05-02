package org.aksw.jenax.arq.connection.link;


public abstract class QueryExecFactoryQueryDecoratorBase<T extends QueryExecFactoryQuery>
	implements QueryExecFactoryQuery
{
	protected T decoratee;

	public QueryExecFactoryQueryDecoratorBase(T decoratee) {
		super();
		this.decoratee = decoratee;
	}
}
