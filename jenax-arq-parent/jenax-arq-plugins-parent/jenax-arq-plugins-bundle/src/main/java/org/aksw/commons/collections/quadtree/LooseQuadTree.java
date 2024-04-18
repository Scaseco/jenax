package org.aksw.commons.collections.quadtree;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;

/**
 * A LooseQuadTree data structure.
 *
 * @param bounds Maximum bounds (e.g. (-180, -90) - (180, 90) for spanning the all wgs84 coordinates)
 * @param maxDepth Maximum depth of the tree
 * @param k The factor controlling the additional size of nodes in contrast to classic QuadTrees.
 * @returns {QuadTree}
 */
public class LooseQuadTree<T> {
    protected double k;
    protected QuadTreeNode<T> rootNode;
    // protected G data;

    /** Map where objects have been inserted */
    protected Map<Object, QuadTreeNode<T>> idToNodes;

    public LooseQuadTree(Envelope bounds, int maxDepth, float k) {
        this.k = k;
        // this.k = k == null ? 0.25 : k;
        this.rootNode = new QuadTreeNode<>(null, 0, bounds, maxDepth, 0, k);

        // Map in which nodes objects with a certain ID are located
        // Each ID may be associated with a set of geometries
        this.idToNodes = new LinkedHashMap<>();
    }

    public QuadTreeNode<T> getRootNode() {
        return rootNode;
    }

//    public G getData() {
//        return data;
//    }

//    public void setData(G data) {
//        this.data = data;
//    }

    /**
     * Retrieve the node that completely encompasses the given bounds
     *
     *
     * @param bounds
     */
    public Collection<QuadTreeNode<T>> acquireNodes(Envelope bounds, int depth) {
        return rootNode.acquireNodes(bounds, depth);
    }


    public List<?> query(Envelope bounds, int depth) {
        return rootNode.query(bounds, depth);
    }
}
