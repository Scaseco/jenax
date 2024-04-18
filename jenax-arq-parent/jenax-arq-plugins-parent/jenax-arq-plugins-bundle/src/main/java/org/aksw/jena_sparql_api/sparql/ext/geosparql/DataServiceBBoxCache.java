package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.commons.collections.quadtree.LooseQuadTree;
import org.aksw.commons.collections.quadtree.QuadTreeNode;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.locationtech.jts.geom.Envelope;

import com.google.common.collect.Sets;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


@RdfType
interface TileCluster
    extends Resource
{
    @HashId
    String getKey();
    TileCluster setKey(String key);

    @Iri("http://www.example.org/zoomClusterBounds")
    GeometryWrapper getZoomClusterBounds();
    TileCluster setZoomClusterBounds(GeometryWrapper bounds);
}

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

    // A cold flowable that can count up to a threshold of items
    protected Single<Boolean> checkedGlobalCount;

    protected long maxGlobalItemCount;
    protected long maxItemsPerTileCount;
    protected int acquireDepth;

    protected Map<Object, Single<QuadTreeNode<T>>> runningTasks = new ConcurrentHashMap<>();

    /** Future whether there are few enough items to fetch them all at once */
    protected CompletableFuture<Boolean> checkedFetchAllAtOnce;

    public DataServiceBBoxCache(MapService<Envelope, T, T> listServiceBBox, long maxGlobalItemCount, long maxItemsPerTileCount, int acquireDepth) {
        this.listServiceBBox = listServiceBBox;

        Envelope maxBounds = new Envelope(-180.0, 180.0, -90.0, 90.0);
        this.quadTree = new LooseQuadTree<>(maxBounds, 18, 0); // 0.5f);

        this.maxItemsPerTileCount = maxItemsPerTileCount; // || 25;
        this.maxGlobalItemCount = maxGlobalItemCount; // || 50;
        this.acquireDepth = acquireDepth; // || 2;


        resetGlobalCount();
    }

    /** Sets checkedGlobalCount to a fresh cold single that upon subscription counts items */
    protected synchronized void resetGlobalCount() {
        checkedGlobalCount = listServiceBBox.createPaginator(null).fetchCount(null, null)
            .map(range -> {
                CountInfo countInfo = RangeUtils.toCountInfo(range);
                boolean canUseGlobal = countInfo.getCount() < maxGlobalItemCount; //  && !countInfo.isHasMoreItems();
                return canUseGlobal;
            })
            .doOnError(t -> {
                resetGlobalCount();
            })
            .doOnSuccess(range -> {
                // checkedGlobal = true;
                // return canUseGlobal;
            });
        // checkedGlobalCount.is
    }

    // TODO: limit and offset currently ignored
    // The immediate result should be a flow of nodes
    // A post processor can then flat map nodes to items or clusters
    public Flowable<Resource> fetchData(Envelope bounds) {
        Flowable<Resource> result = runWorkflow(bounds).flatMapIterable(node -> {
            // TODO Create a function nodeToItemsOrCluster
            List<Resource> r = new ArrayList<>();
//            var arrayOfDocs = nodes.map(function(node) {
//                return node.data.docs;
//            });

            // Remove null items
//            var docs = arrayOfDocs.filter(function(item) {
//                return item;
//            });
            // docs = flatten(docs, true);

            // Add clusters as regular items to the list???
            if (!node.isLoaded()) {
                String wktStr = GeoExprUtils.boundsToWkt(node.getBounds());
                GeometryWrapper geom = WKTDatatype.INSTANCE.read(wktStr);
                TileCluster cluster = ModelFactory.createDefaultModel().createResource().as(TileCluster.class)
                    .setKey(wktStr)
                    .setZoomClusterBounds(geom);

                r.add(cluster);
            }
            return r;
        });

        return result;
    }
    /*
fetchCount: function(bounds, threshold) {
        var result = this.listServiceBbox.fetchCount(bounds, threshold);
        return result;
};
*/
//    public Single<Boolean> runCheckGlobal() {
//        Single<Boolean> result;
//        if (checkedGlobalCount == null) {
//            listServiceBBox.createPaginator(null).fetchCount(null, null)
//                .doOnSuccess(range -> {
//                    CountInfo countInfo = RangeUtils.toCountInfo(range);
//                    boolean canUseGlobal = !countInfo.isHasMoreItems();
//                    checkedGlobal = true;
//                });
//        }
//        return result;
//    }

    public Flowable<QuadTreeNode<T>> runWorkflow(Envelope bounds) {
        QuadTreeNode<T> rootNode = quadTree.getRootNode();
        Flowable<QuadTreeNode<T>> result = checkedGlobalCount.flatMapPublisher(canUseGlobal -> {
            Flowable<QuadTreeNode<T>> task = canUseGlobal
                    ? runGlobalWorkflow(rootNode).toFlowable()
                    : runTiledWorkflow(bounds)
                    ;
            return task;
        });
        return result;
    }

    public Single<QuadTreeNode<T>> runGlobalWorkflow(QuadTreeNode<T> node) {
        Single<QuadTreeNode<T>> result = listServiceBBox.createPaginator(node.getBounds()).toMap().map(items -> {
            // console.log("Global fetching: ", geomToFeatureCount);
            loadTaskAction(node, items);

            // Return the node that was loaded
            return node;
        });

        return result;
    }

    /**
     * This method implements the primary workflow for tile-based fetching
     * data.
     *
     * globalGeomCount = number of geoms - facets enabled, bounds disabled.
     * if(globalGeomCount > threshold) {
     *
     *
     * nodes = aquire nodes. foreach(node in nodes) { fetchGeomCount in the
     * node - facets TODO enabled or disabled?
     *
     * nonFullNodes = nodes where geomCount < threshold foreach(node in
     * nonFullNodes) { fetch geomToFeatureCount - facets enabled
     *
     * fetch all positions of geometries in that area -- Optionally:
     * fetchGeomToFeatureCount - facets disabled - this can be cached per
     * type of interest!! } } }
     *
     */
    public Flowable<QuadTreeNode<T>> runTiledWorkflow(Envelope bounds) {
        // console.log("Aquiring nodes for " + bounds);
        Collection<QuadTreeNode<T>> nodes = quadTree.acquireNodes(bounds, acquireDepth);

        // Mark empty (by inference) nodes as loaded
        for (QuadTreeNode<T> node : nodes) {
            if (node.isCountComplete() && node.getInfMinItemCount() == 0) {
                node.setLoaded(true);
            }
        }

        Flowable<QuadTreeNode<T>> result = Flowable.fromIterable(nodes).flatMapSingle(node -> {
            Single<QuadTreeNode<T>> r = isCountingNeeded(node)
                    ? createCountTask(node)
                    : Single.just(node);
            return r;
        }).flatMapSingle(node -> {
            boolean doLoad = isLoadingNeeded(node);
            return doLoad
                ? createLoadTask(node)
                : Single.just(node);
//      });
//
//      var loadTasks = self.createLoadTasks(nonLoadedNodes);

        });

//        List<QuadTreeNode<T>> uncountedNodes = nodes.stream()
//                .filter(this::isCountingNeeded)
//                .collect(Collectors.toList());
//
//        Flowable<CountInfo> countTasks = createCountTasks(uncountedNodes);

//        var result = countTasks.toList().map(counts -> {
//            var nonLoadedNodes = nodes.filter(function(node) {
//                return self.isLoadingNeeded(node);
//            });
//
//            var loadTasks = self.createLoadTasks(nonLoadedNodes);
//            return PromiseUtils.all(loadTasks).then(function() {
//                return nodes;
//            });
//        });

        return result;
    }

    /** For a given node, this method returns a single that upon completion has the item count set */
    public Single<QuadTreeNode<T>> createCountTask(QuadTreeNode<T> node) {
        Single<QuadTreeNode<T>> countPromise = !isCountingNeeded(node)
                ? Single.just(node)
                : runningTasks.computeIfAbsent(node, n ->
                    listServiceBBox.createPaginator(node.getBounds())
                        .fetchCount(maxItemsPerTileCount, null)
                        .map(RangeUtils::toCountInfo)
                        .map(itemCountInfo -> {
                            long itemCount = itemCountInfo.getCount();
                            node.setMinItemCount(itemCount);
                            // node.setLoaded(true);
                            return node;
                        })
                        .doAfterTerminate(() -> { runningTasks.remove(node); }));
        return countPromise;
    }

    /**
     * If either the minimum number of items in the node is above the
     * threshold or all children have been counted, then there is NO need
     * for counting
     *
     */
    public boolean isCountingNeeded(QuadTreeNode<T> node) {
        // console.log("Node To Count:", node, node.isCountComplete());
        return !(this.isTooManyGeoms(node) || node.isCountComplete());
    }

    /**
     * Loading is needed if NONE of the following criteria applies: . node
     * was already loaded . there are no items in the node . there are to
     * many items in the node
     *
     */
    boolean isLoadingNeeded(QuadTreeNode<T> node) {
        // (node.data && node.data.isLoaded)
        var noLoadingNeeded = node.isLoaded() || (node.isCountComplete() && node.getInfMinItemCount() == 0) || isTooManyGeoms(node);

        return !noLoadingNeeded;
    }

    public boolean isTooManyGeoms(QuadTreeNode<T> node) {
        // console.log("FFS", node.infMinItemCount, node.getMinItemCount());
        return node.getInfMinItemCount() >= this.maxItemsPerTileCount;
    }

//    public Flowable<CountInfo> createCountTasks(Collection<QuadTreeNode<?>> nodes) {
//        Flowable<CountInfo> result = Flowable.fromIterable(nodes)
//                .concatMapSingle(this::createCountTask);
//
////            return self.createCountTask(node);
////        }).filter(function(item) {
////            return item;
////        });
//
//        return result;
//    }

    /**
     * Sets the node's state to loaded, attaches the geomToFeatureCount to
     * it.
     *
     * @param {Object} node
     * //FIXME: @param {Object} geomToFeatureCount
     */
    public void loadTaskAction(QuadTreeNode<T> node, Map<T, T> items) {
        // console.log('Data for ' + node.getBounds() + ': ', docs);
        // node.data.docs = items;
        // node.isLoaded = true;
        for (Entry<T, T> item : items.entrySet()) {
            node.addItem(item.getValue());
            // Envelope bounds = null;
            // node.addItem(items, bounds);
        }
        node.setLoaded(true);
    }

    /** Create a tasks that fetches data for the given nodes */
    Single<QuadTreeNode<T>> createLoadTask(QuadTreeNode<T> node) {
        return Single.just(node).map(n -> {
            Map<T, T> map = listServiceBBox.createPaginator(n.getBounds()).fetchMap();
            loadTaskAction(n, map);
            return n;
        });
    }

    /**
     * TODO tryMergeNode is not yet implemented!
     *
     * This method checks whether there are leafNodes that can be merged into their parents.
     * The collection passed as the argument is left unchanged, however the nodes in it may become detached.
     *
     */
    public List<QuadTreeNode<T>> finalizeLoading(Collection<QuadTreeNode<T>> leafNodes) {
        // Index the nodes by their depth
        NavigableMap<Integer, Set<QuadTreeNode<T>>> depthToNodes = new TreeMap<>();
        for (QuadTreeNode<T> node : leafNodes) {
            int depth = node.getDepth();
            depthToNodes.computeIfAbsent(depth, d -> Sets.newIdentityHashSet()).add(node);
        }

        // Check all levels for nodes that can be merged
        // A merged node is added to the next higher level in order to have it examined again
        int maxDepth = depthToNodes.lastKey();
        Set<QuadTreeNode<T>> mergedParents = Sets.newIdentityHashSet();
        for (int i = maxDepth; i >= 0; --i) {
            Set<QuadTreeNode<T>> nodes = depthToNodes.get(i);
            for (QuadTreeNode<T> node : nodes) {
                QuadTreeNode<T> parent = node.getParent();
                if (parent != null) {
                    // Skip nodes whose parents have already been merged
                    if (mergedParents.contains(parent)) {
                        continue;
                    }
                    boolean didMerge = tryMergeNode(parent);
                    if (didMerge) {
                        mergedParents.add(parent);
                        // Add the merged parent to the previous level (if it isn't already there)
                        depthToNodes.computeIfAbsent(i - 1, d -> Sets.newIdentityHashSet())
                            .add(parent);
                        // Note: We don't spend time to remove children of merged parents from depthToNodes
                    }
                }
            }
        }

        Set<QuadTreeNode<T>> tmp = new LinkedHashSet<>(leafNodes.size());
        for (QuadTreeNode<T> node : leafNodes) {
            // Find the merged parent whose parent isn't merged
            // TODO The logic here is flawed - we need to know whether to add a node itself or its merged ancestor with least depth
            QuadTreeNode<T> parent = node.getParent();
            QuadTreeNode<T> mergedAncestor = parent;
            while (mergedParents.contains(mergedAncestor)) {
                QuadTreeNode<T> q = mergedAncestor.getParent();
                if (q == null) {
                    break;
                }
                mergedAncestor = q;
            }

            if (parent == null || parent == mergedAncestor) {
                // node didn't have a parent, or the parent was not merged - add node
                tmp.add(node);
            } else {
                // ancestors of node were merged
                tmp.add(mergedAncestor);
            }
        }

        List<QuadTreeNode<T>> result = new ArrayList<>(tmp);
        return result;
    }

    /**
     * If all child nodes are loaded and the num of all items is less than the threshold then
     * copy all items to this node and remove the children.
     */
    public boolean tryMergeNode(QuadTreeNode<T> node) {
        // TODO Implement
        return false;
    }
}

//
//
////    public void create() {
////        // return
////    }
//
//  public Single<T> scheduleTask(Object id, Callable<T> task) {
//  runningTasks.computeIfAbsent(id, x -> {
//      return Single.fromCallable(task);
//  });
//}
//protected Map<QuadTreeNode<T>, Single<Long>> countTasks;
//protected Map<QuadTreeNode<T>, > dataFetchingTasks;
//
//

