package org.aksw.facete.v3.experimental;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.apache.jena.rdf.model.Resource;

public abstract class PathMultiNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
    implements TraversalMultiNode<N>
{
    protected D parent;
    protected boolean isFwd;
    protected Resource property;
    //protected boolean isFwd;
    //protected Resource property;
    protected Map<String, N> aliasToNode = new LinkedHashMap<>();


    public PathMultiNode(D parent, Resource property) {
        super();
        this.parent = parent;
        this.isFwd = parent.isFwd();
        this.property = property;
    }

    @Override
    public N viaAlias(String alias) {
        N result = aliasToNode.computeIfAbsent(alias, a -> {
            // Expanded for easier debugging
            return this.viaImpl(a);
        });
        return result;
    }

    @Override
    public Map<String, N> list() {
        return aliasToNode;
    }

    protected abstract N viaImpl(String alias);
}
