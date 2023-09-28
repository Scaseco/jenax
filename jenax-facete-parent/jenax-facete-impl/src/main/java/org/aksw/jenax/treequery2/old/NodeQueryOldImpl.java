package org.aksw.jenax.treequery2.old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.facete.treequery2.api.OrderNode;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryBase;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.query.SortCondition;

public class NodeQueryOldImpl
    extends NodeQueryBase
    implements NodeQueryOld
{
    protected List<SortCondition> sortConditions = new ArrayList<>();
    protected Map<FacetStep, NodeQueryOld> children = new LinkedHashMap<>();

    protected FacetStep step;
    protected NodeQueryOld parent;
    protected Long offset;
    protected Long limit;

    public NodeQueryOldImpl(NodeQueryOld parent, FacetStep step) {
        super();
        this.parent = parent;
        this.step = step;
    }

    @Override
    public Long offset() {
        return offset;
    }

    @Override
    public NodeQueryOld offset(Long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Long limit() {
        return limit;
    }

    @Override
    public NodeQueryOld limit(Long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public NodeQueryOld getParent() {
        return parent;
    }

    @Override
    public FacetPath getPath() {
        FacetPath result = parent == null
                ? FacetPath.newAbsolutePath(step == null ? Collections.emptyList() : Collections.singletonList(step))
                : parent.getPath().resolve(step);
        return result;
    }

    @Override
    public Collection<NodeQueryOld> getChildren() {
        return children.values();
    }

    @Override
    public List<SortCondition> getSortConditions() {
        return sortConditions;
    }

    @Override
    public NodeQueryOld getChild(FacetStep step) {
        return children.get(step);
    }

    @Override
    public NodeQueryOld getOrCreateChild(FacetStep step) {
        NodeQueryOld result = children.computeIfAbsent(step, s -> new NodeQueryOldImpl(this, s));
        return result;
    }

    public static NodeQueryOld newRoot() {
        return new NodeQueryOldImpl(null, null);
    }

    /** Start a traversal for a order. Orders are only applied when .asc() or .desc() is called. */
    @Override
    public OrderNode order() {
        return null;
    }

    @Override
    public RelationQuery getRelation() {
        // TODO Auto-generated method stub
        return null;
    }
}
