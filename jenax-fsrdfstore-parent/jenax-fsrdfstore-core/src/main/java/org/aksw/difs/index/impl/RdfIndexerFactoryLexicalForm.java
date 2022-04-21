package org.aksw.difs.index.impl;

import java.util.function.Function;

import org.aksw.difs.index.api.RdfTermIndexerFactory;
import org.apache.jena.graph.Node;

public class RdfIndexerFactoryLexicalForm
	implements RdfTermIndexerFactory
{
	@Override
	public Function<Node, String[]> getMapper() {
		return DatasetGraphIndexerFromFileSystem::iriOrLexicalFormToPath;
	}
}
