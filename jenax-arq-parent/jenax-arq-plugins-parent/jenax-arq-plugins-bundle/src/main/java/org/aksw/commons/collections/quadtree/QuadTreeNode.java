package org.aksw.commons.collections.quadtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

//var Class = require('../ext/Class');
//var Point = require('./Point');
//var Bounds = require('./Bounds');
//var reduce = require('lodash.reduce');


public class QuadTreeNode<T> {
    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_LEFT = 2;
    public static final int BOTTOM_RIGHT = 3;

    protected QuadTreeNode<T> parent;
    protected QuadTreeNode<T>[] children; // Either null or an array of length  4

    protected final int parentChildIndex; // By which index the parent refers to this node {null, 0-3}

    protected final Envelope bounds;

    // XXX Make global attributes part of the tree rather than copying into each node?
    protected final int maxDepth;
    protected final float k;

    protected final int depth;
    protected boolean isLoaded;

    /** Explicit number of minimum items set on this node */
    protected Long minItemCount;

    /** Inferred minimum item count (recurses to the parents) */
    protected long infMinItemCount;


    protected Set<T> data;
//     protected Map<Object, T> data;
//    protected Map<Object, Envelope> idToPos;

    public QuadTreeNode(QuadTreeNode<T> parent, int parentChildIndex, Envelope bounds, int maxDepth, int depth, float k) { //, parentChildIndex) {
        this.parent = parent;
        // this.parentChildIndex = parentChildIndex;

        this.bounds = bounds;
        this.parentChildIndex = parentChildIndex;
        this.maxDepth = maxDepth;
        this.depth = depth;
        this.k = k;  // expansion factor for loose quad tree [0, 1[ - recommended range: 0.25-0.5

        this.isLoaded = false;
        this.children = null;

        // this.data = new LinkedHashMap<>();

        this.minItemCount = null; // Concrete minimum item count
        this.infMinItemCount = 0; // Inferred minimum item count by taking the sum

        // The contained items: id->position (so each item must have an id)
        // this.idToPos = new LinkedHashMap<>();
    }

    public QuadTreeNode<T> getParent() {
        return parent;
    }

    public QuadTreeNode<T>[] getChildren() {
        return children;
    }

    public Set<T> getData() {
        return data == null ? Collections.emptySet() : data;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public long getInfMinItemCount() {
        return infMinItemCount;
    }

    public int getDepth() {
        return depth;
    }

    /** Build a string that represents the path to this node from the root */
    public String getId() {
        var parent = this.parent;
        var parentId = parent != null ? parent.getId() : "";

        var indexId = parent != null ? Integer.toString(parentChildIndex) : "r"; // r for root
        var result = parentId + indexId;
        return result;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public void addItem(T item) {
        if (data == null) {
            data = new LinkedHashSet<>();
        }

        data.add(item);
//        idToPos.put(id, pos);
    }

//    public void addItems(Map<Object, Envelope> idToPos) {
//        idToPos.forEach(this::addItem);
//        // (id, env) -> addItem(id, env));
////        for(Object id : idToPos.keySet()) {
////            // Envelope pos = idToPos.get$[id];
////
////            this.addItem(id, pos);
////        }
//    }

    public void removeItem(Object id) {
        data.remove(id);
        // idToPos.remove(id);
    }

    /**
     * Sets the minimum item count on this node and recursively updates
     * the inferred minimum item count (.infMinItemCount) on its parents.
     *
     * @param value
     */
    public void setMinItemCount(long value) {
        minItemCount = value;
        infMinItemCount = value;

        if(parent != null) {
            parent.updateInfMinItemCount();
        }
    }

    public Long getMinItemCount() {
        return minItemCount;
    }

    /**
     * True if either the minItemCount is set, or all children have it set
     * FIXME This description is not concise - mention the transitivity
     *
     * @returns
     */
    public boolean isCountComplete() {
        boolean result = false;
        if (getMinItemCount() != null && children == null) {
            result = true;
        } else if(children != null) {
            result = Arrays.asList(children).stream().allMatch(QuadTreeNode::isCountComplete);
        }
        return result;
    }

    /**
     * Update the inferred minimum item count of this node.
     * It's the sum of the children's inferred minimum item counts.
     */
    public void updateInfMinItemCount() {
        if(this.children == null && this.minItemCount != null) {
            return;
        }
        long sum = 0;
        for (QuadTreeNode<T> child : children) {
            if (child.getMinItemCount() != null) {
                sum += child.getMinItemCount();
            } else { // if (child.infMinItemCount != null) {
                sum += child.getMinItemCount();
            }
        }

        infMinItemCount = sum;

        if (parent != null) {
            this.parent.updateInfMinItemCount();
        }
    }

    public Envelope getBounds() {
        return bounds;
    }

    public Coordinate getCenter() {
        return bounds.centre();
    }

    public QuadTreeNode<T> newNode(int parentChildIndex, Envelope bounds) {
        return new QuadTreeNode<>(parent, parentChildIndex, bounds, maxDepth, depth + 1, k);
    }

    public void subdivide() {
        Coordinate c = this.getCenter();

        // expansions
        double ew = k * 0.5 * bounds.getWidth();
        double eh = k * 0.5 * bounds.getHeight();

        children = new QuadTreeNode[4];

        children[QuadTreeNode.TOP_LEFT] = newNode(QuadTreeNode.TOP_LEFT, new Envelope(
            bounds.getMinX(),
            c.x + ew,
            c.y - eh,
            bounds.getMaxY())
        );

        children[QuadTreeNode.TOP_RIGHT] = newNode(QuadTreeNode.TOP_RIGHT, new Envelope(
            c.x - ew,
            bounds.getMaxX(),
            c.y - eh,
            bounds.getMaxY()
        ));

        children[QuadTreeNode.BOTTOM_LEFT] = newNode(QuadTreeNode.BOTTOM_LEFT, new Envelope(
            bounds.getMinX(),
            c.x + ew,
            bounds.getMinY(),
            c.y + eh
        ));

        children[QuadTreeNode.BOTTOM_RIGHT] = newNode(QuadTreeNode.BOTTOM_RIGHT, new Envelope(
            c.x - ew,
            bounds.getMaxX(),
            bounds.getMinY(),
            c.y + eh
        ));

        // Uncomment for debug output
        /*
        console.log("Subdivided " + this._bounds + " into ");
        for(var i in this.children) {
            var child = this.children[i];
            console.log("    " + child._bounds);
        }
        */
    }

    /**
     * Return loaded and leaf nodes within the bounds
     *
     * @param bounds
     * @param depth The maximum number of levels to go beyond the level derived from the size of bounds
     * @returns {Array}
     */
    public List<QuadTreeNode<T>> query(Envelope bounds, int depth) {
        List<QuadTreeNode<T>> result = new ArrayList<>();
        this.queryRec(bounds, result, depth);
        return result;
    }

    public void queryRec(Envelope queryBounds, Collection<QuadTreeNode<T>> result, int depth) {
        if (!bounds.intersects(queryBounds)) {
            return;
        }

        double w = queryBounds.getWidth() / bounds.getWidth();
        double h = queryBounds.getHeight() / bounds.getHeight();

        double r = Math.max(w, h);

        // Stop recursion on encounter of a loaded node or leaf node or node that exceeded the depth limit
        if (this.isLoaded || this.children == null || r >= depth) {
            result.add(this);
            return;
        }

        for(QuadTreeNode<T> child : children) {
            // FIXME: depth is not defined
            child.queryRec(bounds, result, depth);
        }
    }

    /**
     * If the node'size is above a certain ratio of the size of the bounds,
     * it is placed into result. Otherwise, it is recursively split until
     * the child nodes' ratio to given bounds has become large enough.
     *
     * Use example:
     * If the screen is centered on a certain location, then this method
     * picks tiles (quad-tree-nodes) of appropriate size (not too big and not too small).
     *
     *
     * @param bounds
     * @param depth
     * @param result
     */
    public void splitFor(Envelope splitBounds, int depth, Collection<QuadTreeNode<T>> result) {
        /*
        console.log("Depth = " + depth);
        console.log(this.getBounds());
        */


        /*
        if(depth > 10) {
            result.push(this);
            return;
        }*/


        if (!bounds.intersects(splitBounds)) {
            return;
        }

        // If the node is loaded, avoid splitting it
        if (isLoaded()) {
            if(result != null) {
                result.add(this);
            }
            return;
        }

        // How many times the current node is bigger than the view rect
        double w = splitBounds.getWidth() / bounds.getWidth();
        double h = splitBounds.getHeight() / bounds.getHeight();

        double r = Math.max(w, h);
        //var r = Math.min(w, h);

        if (r >= depth || this.depth >= this.maxDepth) {
            if (result != null) {
                result.add(this);
                System.out.println("Added a node:" + this.getBounds() + " - " + this.depth + " - " + r);
            }
            return;
        }

        if(this.children == null) {
            subdivide();
        }

        for (int i = 0; i < children.length; ++i) {
            QuadTreeNode<T> child = children[i];

            //console.log("Split for ",child, bounds);
            child.splitFor(splitBounds, depth, result);
        }
    }

    /** Returns the collection of leaf nodes for the given bounds */
    public Collection<QuadTreeNode<T>> acquireNodes(Envelope splitBounds, int depth) {
        List<QuadTreeNode<T>> result = new ArrayList<>();

        splitFor(splitBounds, depth, result);

        return result;
    }

    /** If there is a parent, replace this node with an empty one */
    public void unlink() {
        if (this.parent != null) {
            parent.children[parentChildIndex] = newNode(parentChildIndex, bounds);
        }
    }

    @Override
    public String toString() {
        return "QuadTreeNode(" + getBounds() + ", itemCount: " + minItemCount + ", infMinItemCount: " + infMinItemCount + ")";
    }
}



//_findIndexPoint: function(point) {
//// FIXME: bounds not defined
//  var center = this.getCenter(bounds);
//  var left = point.x < center.x;
//  var top = point.y > center.y;
//
//  var index;
//  if(left) {
//      if(top) {
//          index = Node.TOP_LEFT;
//      } else {
//          index = Node.BOTTOM_LEFT;
//      }
//  } else {
//      if(top) {
//          index = Node.TOP_RIGHT;
//      } else {
//          index = Node.BOTTOM_RIGHT;
//      }
//  }
//
//  return index;
//},

//_findIndex: function(bounds) {
//  var topLeft = new Point(bounds.left, bounds.top);
//  return this._findIndexPoint(topLeft);
//},

//getOverlaps: function(bounds) {
//
//}

