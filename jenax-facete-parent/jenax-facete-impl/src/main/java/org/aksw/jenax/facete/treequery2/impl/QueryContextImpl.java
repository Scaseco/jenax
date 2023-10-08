package org.aksw.jenax.facete.treequery2.impl;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v4.impl.PropertyResolver;
import org.aksw.jenax.facete.treequery2.api.FacetPathMapping;
import org.aksw.jenax.facete.treequery2.api.QueryContext;
import org.apache.jena.sparql.core.Var;

public class QueryContextImpl
    implements QueryContext
{
    protected PropertyResolver propertyResolver;
    protected FacetPathMapping facetPathMapping = new FacetPathMappingImpl();
    protected Generator<String> scopeNameGenerator = Generator.create("sc");

    protected Set<Var> usedVars = new HashSet<>(); // synchronized?

    public QueryContextImpl(PropertyResolver propertyResolver) {
        super();
        this.propertyResolver = propertyResolver;
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public Set<Var> getUsedVars() {
        return usedVars;
    }

    @Override
	public FacetPathMapping getPathMapping() {
		return facetPathMapping;
	}

    @Override
    public Generator<String> getFieldIdGenerator() {
        return scopeNameGenerator;

    }
}
