package org.aksw.jenax.connection.dataengine;

import org.aksw.commons.util.closeable.AutoCloseableDecoratorBase;
import org.apache.jena.rdfconnection.RDFConnection;

public class RdfDataEngineDecoratorBase<T extends RdfDataEngine>
	extends AutoCloseableDecoratorBase<T>
	implements RdfDataEngine
{
	public RdfDataEngineDecoratorBase(T decoratee) {
		super(decoratee);
	}

	@Override
	public RDFConnection getConnection() {
		return decoratee.getConnection();
	}
}
