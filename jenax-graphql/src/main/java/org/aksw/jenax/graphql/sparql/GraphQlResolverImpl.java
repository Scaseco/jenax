package org.aksw.jenax.graphql.sparql;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.model.shacl.util.ShUtils;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.collect.Multimap;

public class GraphQlResolverImpl
    implements GraphQlResolver
{
    protected VoidDataset voidDataset;
    protected Model shaclModel;
    protected Multimap<P_Path0, ShPropertyShape> globalPropertyShapes;

    public GraphQlResolverImpl(VoidDataset voidDataset, Model shaclModel) {
        super();
        this.voidDataset = voidDataset;
        this.shaclModel = shaclModel;

        globalPropertyShapes = ShUtils.indexGlobalPropertyShapes(shaclModel);
    }

    public Multimap<P_Path0, ShPropertyShape> getGlobalPropertyShapes() {
        return globalPropertyShapes;
    }

    @Override
    public Collection<ShPropertyShape> getGlobalPropertyShapes(P_Path0 path) {
        return globalPropertyShapes.get(path);
    }

    @Override
    public Set<org.apache.jena.graph.Node> resolveKeyToClasses(String key) {
        // TODO Index and/or cache
        Set<org.apache.jena.graph.Node> classes = voidDataset.getClassPartitionMap().keySet();
        Set<org.apache.jena.graph.Node> result = classes.stream()
                .filter(org.apache.jena.graph.Node::isURI)
                .filter(node -> node.getLocalName().equals(key))
                .collect(Collectors.toSet());
        return result;
    }

    @Override
    public FacetPath resolveKeyToProperty(String rawKey) {
        // TODO Try to resolve the key name - if it fails try again by removing the inverse prefix
        boolean isFwd = !rawKey.startsWith("inv_");
        String key = isFwd ? rawKey : rawKey.substring(1);

        FacetPath result;
        if (Objects.equals(key, GraphQlSpecialKeys.xid)) {
            result = FacetPath.newRelativePath();
        } else {
            // TODO Index and/or cache
            Set<org.apache.jena.graph.Node> allProperties = voidDataset.getPropertyPartitionMap().keySet();
            List<org.apache.jena.graph.Node> matchingProperties = allProperties.stream()
                    .filter(org.apache.jena.graph.Node::isURI)
                    .filter(node -> node.getLocalName().equals(key))
                    .collect(Collectors.toList());

            org.apache.jena.graph.Node p = IterableUtils.expectZeroOrOneItems(matchingProperties);
            if (p != null) {
                FacetStep step = FacetStep.of(p, Direction.ofFwd(isFwd), "", FacetStep.TARGET);
                result = FacetPath.newRelativePath(step);
                // nq.resolve(FacetPath.newAbsolutePath().resolve(Fac))
            } else {
                result = null;
            }
        }
        return result;
    }
}
