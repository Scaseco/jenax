package org.aksw.jenax.graphql.sparql;

import java.util.Collection;
import java.util.Set;

import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public interface GraphQlResolverWrapper
    extends GraphQlResolver
{
    GraphQlResolver getDelegate();

    @Override
    default Set<Node> resolveKeyToClasses(String key) {
        return getDelegate().resolveKeyToClasses(key);
    }

    @Override
    default FacetPath resolveKeyToProperty(String key) {
        return getDelegate().resolveKeyToProperty(key);
    }

    @Override
    default Collection<ShPropertyShape> getGlobalPropertyShapes(P_Path0 path) {
        return getDelegate().getGlobalPropertyShapes(path);
    }
}
