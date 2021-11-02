package org.aksw.facete.v3.api.path;

public abstract class ResolverBase
	implements Resolver
{
	protected Resolver parent;
	
	public ResolverBase(Resolver parent) {
		this.parent = parent;
	}
	
	@Override
	public Resolver getParent() {
		return parent;
	}
}
