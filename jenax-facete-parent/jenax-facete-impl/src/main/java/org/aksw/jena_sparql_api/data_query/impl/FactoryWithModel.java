package org.aksw.jena_sparql_api.data_query.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.base.Supplier;

public class FactoryWithModel<T extends RDFNode>
	implements Supplier<T>
{
	protected Model model;
	protected Class<T> clazz;


	public FactoryWithModel(Class<T> clazz) {
		this(clazz, ModelFactory.createDefaultModel());
	}
	
	public FactoryWithModel(Class<T> clazz, Model model) {
		super();
		this.model = model;
		this.clazz = clazz;
	}

	@Override
	public T get() {
		T result = model.createResource().as(clazz);
		return result;
	}

	public Model getModel() {
		return model;
	}
	
}
