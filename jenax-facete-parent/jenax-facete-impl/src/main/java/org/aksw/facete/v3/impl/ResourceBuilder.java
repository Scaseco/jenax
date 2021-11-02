package org.aksw.facete.v3.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class ResourceBuilder<P> {
	protected P parent;
	protected Resource resource;
	
	public ResourceBuilder(P parent) {
		this.parent = parent;
	}
	
	public ResourceBuilder<P> setModel(Model model) {
		setResource(model.createResource());
		return this;
	}
	
	public ResourceBuilder<P> setResource(Resource resource) {
		this.resource = resource;
		return this;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public P end() {
		return parent;
	}
}
