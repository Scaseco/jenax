package org.aksw.facete.v3.experimental;

import java.util.LinkedHashMap;

import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

public abstract class PathDirNode<N, M extends TraversalMultiNode<N>>
    implements TraversalDirNode<N, M>
{
    protected N parent;
    protected boolean isFwd;
    // protected Map<Resource, M> propToMultiNode = new LinkedHashMap<>();
    protected Table<Resource, Integer, M> propComponentToMultiNode = Tables.newCustomTable(new LinkedHashMap<>(), LinkedHashMap::new); // HashBasedTable.

    public PathDirNode(N parent, boolean isFwd) {
        super();
        this.parent = parent;
        this.isFwd = isFwd;
    }

    @Override
    public boolean isFwd() {
        return isFwd;
    }

    @Override
    public M via(Resource property, Integer component) {
        M result = propComponentToMultiNode.row(property).computeIfAbsent(component, c -> {
            // Expanded for easier debugging
            return viaImpl(property, c);
        });
        return result;
    }

    protected abstract M viaImpl(Resource property, Integer component);
}
