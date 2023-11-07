package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.concurrent.CompletableFuture;

import org.aksw.commons.collections.quadtree.LooseQuadTree;
import org.aksw.commons.rx.lookup.MapService;
import org.locationtech.jts.geom.Envelope;

/**
 * Adds a quad tree cache to the lookup service.
 *
 * If there are only few items, then the 'global' workflow fetches them all.
 * The 'tiled' workflow starts from the current viewport.
 */
public class DataServiceBBoxCache<C, T>
//    implements ListService<C, T>
{
    protected MapService<Envelope, T, T> listServiceBBox;
    protected LooseQuadTree<T> quadTree;
    protected long maxGlobalItemCount;
    protected long maxItemsPerTileCount;
    protected int aquireDepth;

//    protected Map<QuadTreeNode<T>, Single<Long>> countTasks;
//    protected Map<QuadTreeNode<T>, > dataFetchingTasks;
//
//
    /** Future whether there are few enough items to fetch them all at once */
    protected CompletableFuture<Boolean> checkedFetchAllAtOnce;
//
//
////    public void create() {
////        // return
////    }
//
//    public DataServiceBBoxCache(ListService<Envelope, T> listServiceBbox, long maxGlobalItemCount, long maxItemsPerTileCount, int aquireDepth) {
//        this.listServiceBBox = listServiceBbox;
//
//        Envelope maxBounds = new Envelope(-180.0, -90.0, 180.0, 90.0);
//        this.quadTree = new LooseQuadTree<>(maxBounds, 18, 0.5f);
//
//        this.maxItemsPerTileCount = maxItemsPerTileCount; // || 25;
//        this.maxGlobalItemCount = maxGlobalItemCount; // || 50;
//        this.aquireDepth = aquireDepth; // || 2;
//    }
//
//    // TODO: limit and offset currently ignored
//    void fetchData(Envelope bounds) {
//        var result = this.runWorkflow(bounds).then(function(nodes) {
//            var arrayOfDocs = nodes.map(function(node) {
//                return node.data.docs;
//            });
//
//            // Remove null items
//            var docs = arrayOfDocs.filter(function(item) {
//                return item;
//            });
//            docs = flatten(docs, true);
//
//            // Add clusters as regular items to the list???
//            nodes.forEach(function(node) {
//                if (node.isLoaded) {
//                    return;
//                }
//
//                var wkt = GeoExprUtils.boundsToWkt(node.getBounds());
//
//                var cluster = {
//                    key: wkt,
//                    val: {
//                        id: wkt,
//                        // type: 'cluster',
//                        // isZoomCluster: true,
//                        zoomClusterBounds: node.getBounds(),
//                        wkt: wkt // NodeFactory.createPlainLiteral(
//                    }
//                };
//
//                docs.push(cluster);
//            });
//
//            return docs;
//        });
//
//        return result;
//    }
//    /*
//fetchCount: function(bounds, threshold) {
//        var result = this.listServiceBbox.fetchCount(bounds, threshold);
//        return result;
//};
//*/
//    public void runCheckGlobal() {
//        var result;
//
//        var rootNode = this.quadTree.getRootNode();
//
//        if (!rootNode.checkedGlobal) {
//
//            var globalCountTask = this.listServiceBbox.fetchCount(null, this.maxGlobalItemCount);
////console.log('dammit', this.listServiceBbox);
//            result = globalCountTask.then(function(countInfo) {
//                var canUseGlobal = !countInfo.hasMoreItems;
//                //console.log('Global check counts', countInfo);
//                rootNode.canUseGlobal = canUseGlobal;
//                rootNode.checkedGlobal = true;
//
//                return canUseGlobal;
//            });
//
//        } else {
//            result = Promise.resolve(rootNode.canUseGlobal).cancellable();
//        }
//
//        return result;
//    }
//
//    public CompletableFuture<List<QuadTreeNode<T>>> runWorkflow(Envelope bounds) {
//        var rootNode = this.quadTree.getRootNode();
//
//        var self = this;
//        var result = this.runCheckGlobal().then(function(canUseGlobal) {
//            //console.log('Can use global? ', canUseGlobal);
//            var task;
//            if (canUseGlobal) {
//                task = self.runGlobalWorkflow(rootNode);
//            } else {
//                task = self.runTiledWorkflow(bounds);
//            }
//
//            return task.then(function(nodes) {
//                return nodes;
//            });
//        });
//        return result;
//    }
//
//    public void runGlobalWorkflow(QuadTreeNode<T> node) {
//        var self = this;
//
//        var result = this.listServiceBbox.fetchItems(null).then(function(docs) {
//            // console.log("Global fetching: ", geomToFeatureCount);
//            self.loadTaskAction(node, docs);
//
//            return [
//                node,
//            ];
//        });
//
//        return result;
//    }
//
//    /**
//     * This method implements the primary workflow for tile-based fetching
//     * data.
//     *
//     * globalGeomCount = number of geoms - facets enabled, bounds disabled.
//     * if(globalGeomCount > threshold) {
//     *
//     *
//     * nodes = aquire nodes. foreach(node in nodes) { fetchGeomCount in the
//     * node - facets TODO enabled or disabled?
//     *
//     * nonFullNodes = nodes where geomCount < threshold foreach(node in
//     * nonFullNodes) { fetch geomToFeatureCount - facets enabled
//     *
//     * fetch all positions of geometries in that area -- Optionally:
//     * fetchGeomToFeatureCount - facets disabled - this can be cached per
//     * type of interest!! } } }
//     *
//     */
//    public void runTiledWorkflow(Envelope bounds) {
//        var self = this;
//
//        // console.log("Aquiring nodes for " + bounds);
//        var nodes = this.quadTree.acquireNodes(bounds, this.aquireDepth);
//
//        // console.log('Done aquiring');
//
//        // Init the data attribute if needed
//        nodes.forEach(function(node) {
//            if (!node.data) {
//                node.data = {};
//            }
//        });
//
//        // Mark empty nodes as loaded
//        nodes.forEach(function(node) {
//            if (node.isCountComplete() && node.infMinItemCount === 0) {
//                node.isLoaded = true;
//            }
//        });
//
//        var uncountedNodes = nodes.filter(function(node) {
//            return self.isCountingNeeded(node);
//        });
//
//        var countTasks = this.createCountTasks(uncountedNodes);
//
//        var result = PromiseUtils.all(countTasks).then(function() {
//            var nonLoadedNodes = nodes.filter(function(node) {
//                return self.isLoadingNeeded(node);
//            });
//
//            var loadTasks = self.createLoadTasks(nonLoadedNodes);
//            return PromiseUtils.all(loadTasks).then(function() {
//                return nodes;
//            });
//        });
//
//        return result;
//    }
//
//    public void createCountTask(QuadTreeNode<?> node) {
//
//        var self = this;
//        var threshold = self.maxItemsPerTileCount; // ? self.maxItemsPerTileCount + 1 : null;
//
//        listServiceBBox.createPaginator(null).fetchCount(, null);
//
//        var countPromise = this.listServiceBbox.fetchCount(node.getBounds(), threshold);
//        var result = countPromise.then(function(itemCountInfo) {
//            var itemCount = itemCountInfo.count;
//            node.setMinItemCount(itemCountInfo.count);
//
//            // If the value is 0, also mark the node as loaded
//            if (itemCount === 0) {
//                // self.initNode(node);
//                node.isLoaded = true;
//            }
//        });
//
//        return result;
//    }
//
//    /**
//     * If either the minimum number of items in the node is above the
//     * threshold or all children have been counted, then there is NO need
//     * for counting
//     *
//     */
//    boolean isCountingNeeded(QuadTreeNode<T> node) {
//        // console.log("Node To Count:", node, node.isCountComplete());
//        return !(this.isTooManyGeoms(node) || node.isCountComplete());
//    }
//
//    /**
//     * Loading is needed if NONE of the following criteria applies: . node
//     * was already loaded . there are no items in the node . there are to
//     * many items in the node
//     *
//     */
//    boolean isLoadingNeeded(QuadTreeNode<T> node) {
//
//        // (node.data && node.data.isLoaded)
//        var noLoadingNeeded = node.isLoaded || (node.isCountComplete() && node.infMinItemCount === 0) || this.isTooManyGeoms(node);
//
//        return !noLoadingNeeded;
//    }
//
//    public boolean isTooManyGeoms(QuadTreeNode<T> node) {
//        // console.log("FFS", node.infMinItemCount, node.getMinItemCount());
//        return node.getInfMinItemCount() >= this.maxItemsPerTileCount;
//    }
//
//    public Future<?> createCountTasks(Collection<QuadTreeNode<?>> nodes) {
//        var self = this;
//        var result = nodes.map(function(node) {
//            return self.createCountTask(node);
//        }).filter(function(item) {
//            return item;
//        });
//
//        return result;
//    }
//
//    /**
//     * Sets the node's state to loaded, attaches the geomToFeatureCount to
//     * it.
//     *
//     * @param {Object} node
//     * //FIXME: @param {Object} geomToFeatureCount
//     */
//    loadTaskAction: function(node, docs) {
//        // console.log('Data for ' + node.getBounds() + ': ', docs);
//        node.data.docs = docs;
//        node.isLoaded = true;
//    }
//
//    /** Create a tasks that fetches data for the given nodes */
//    void createLoadTasks(List<QuadTreeNode<T>> nodes) {
//        var self = this;
//        var result = nodes.map(function(node) {
//            var loadTask = self.listServiceBbox.fetchItems(node.getBounds()).then(function(docs) {
//                self.loadTaskAction(node, docs);
//            });
//
//            return loadTask;
//        });
//
//        return result;
//    }
//
//    /**
//     * TODO Finishing this method at some point to merge nodes together
//     * could be useful
//     *
//     */
//    void finalizeLoading(Collection<QuadTreeNode<T>> nodes) {
//        // Restructure all nodes that have been completely loaded,
//        var parents = [];
//
//        nodes.forEach(function(node) {
//            if (node.parent) {function
//                parents.push(node.parent);
//            }
//        });
//
//        parents = uniq(parents);
//
//        var each = function(child) {
//            var indexOf = nodes.indexOf(child);
//            if (indexOf >= 0) {
//                nodes[indexOf] = undefined;
//            }
//        };
//
//        var change = false;
//        do {
//            change = false;
//            for (var i in parents) {
//                var p = parents[i];
//
//                var children = p.children;
//
//                var didMerge = tryMergeNode(p);
//                if (!didMerge) {
//                    continue;
//                }
//
//                change = true;
//
//                children.forEach(each);
//
//                nodes.push(p);
//
//                if (p.parent) {
//                    parents.push(p.parent);
//                }
//
//                break;
//            }
//        } while (change === true);
//
//        nodes = nodes.filter(function(item) {
//            return item;
//        });
//
//        /*
//         * $.each(nodes, function(i, node) { node.isLoaded = true; });
//         */
//
//        // console.log("All done");
//        // self._setNodes(nodes, bounds);
//        // callback.success(nodes, bounds);
//    }
}
