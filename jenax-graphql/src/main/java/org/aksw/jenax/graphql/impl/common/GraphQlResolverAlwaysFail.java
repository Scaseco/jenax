package org.aksw.jenax.graphql.impl.common;

import java.util.Collection;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.GraphQlResolver;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public class GraphQlResolverAlwaysFail
    implements GraphQlResolver
{
    @Override
    public Set<Node> resolveKeyToClasses(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetPath resolveKeyToProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ShPropertyShape> getGlobalPropertyShapes(P_Path0 path) {
        throw new UnsupportedOperationException();
    }
}
