package org.aksw.jena_sparql_api.difs.main;

import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.op.RxOps;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.commons.txn.impl.ContentSync;
import org.aksw.commons.txn.impl.FileSyncImpl;
import org.aksw.difs.index.api.DatasetGraphIndexPlugin;
import org.aksw.jena_sparql_api.difs.txn.SyncedDataset;
import org.aksw.jena_sparql_api.difs.txn.TxnUtils;
import org.aksw.jenax.arq.dataset.diff.DatasetGraphDiff;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import io.reactivex.rxjava3.core.Flowable;


public class DatasetGraphFromTxnMgr
    extends DatasetGraphBase
{
    protected static final Logger logger = LoggerFactory.getLogger(DatasetGraphFromTxnMgr.class);

    protected TxnMgr txnMgr;
    protected boolean useJournal;

    // Whether operations such as dataset loading should run in parallel
    protected boolean isParallel;

    protected ThreadLocal<Txn> txns = ThreadLocal.withInitial(() -> null);

    protected boolean allowEmptyGraphs; // If false then auto-delete empty graphs - if true then DROP GRAPH <foo> is needed.

    protected Collection<DatasetGraphIndexPlugin> indexers = Collections.synchronizedSet(new HashSet<>());

    protected PrefixMap prefixes = PrefixMapFactory.create();

    protected org.aksw.commons.path.core.Path<String> storeBaseSegments;

    protected String dataFileName;

//    public static DatasetGraphFromTxnMgr createDefault(Path repoRoot) {
//        PathMatcher pathMatcher = repoRoot.getFileSystem().getPathMatcher("glob:**/*.trig");
//
//
//        DatasetGraphFromTxnMgr result = new DatasetGraphFromTxnMgr(
//        		repoRoot,
//                pathMatcher,
//                path -> false);
//
//        return result;
//    }


    // TODO Make cache configurable; ctor must accept a cache builder
    protected LoadingCache<org.aksw.commons.path.core.Path<String>, SyncedDataset> syncCache;

    public static LoadingCache<org.aksw.commons.path.core.Path<String>, SyncedDataset> createCache(
            TxnMgr txnMgr,
            boolean allowEmptyGraphs,
            CacheBuilder<org.aksw.commons.path.core.Path<String>, SyncedDataset> cacheBuilder) {
        LoadingCache<org.aksw.commons.path.core.Path<String>, SyncedDataset> result = cacheBuilder
            .removalListener(ev -> {
                logger.info("Cache eviction of dataset graph for " + ev.getKey());
                SyncedDataset sd = (SyncedDataset)ev.getValue();
                sd.save();
            })
            .build(new CacheLoader<org.aksw.commons.path.core.Path<String>, SyncedDataset>() {
                @Override
                public SyncedDataset load(org.aksw.commons.path.core.Path<String> key) throws Exception {
                    logger.info("Loading data at " + key);

                    // org.aksw.commons.path.core.Path<String> key = keyArr;; //.getArray();
                    // ResourceRepository<String> resRepo = txnMgr.getResRepo();
                    // Path rootPath = resRepo.getRootPath();
                    Path rootPath = txnMgr.getRootPath();

                    // Path relPath = r// resRepo.getRelPath(key);
                    Path absPath = PathUtils.resolve(rootPath, key.getSegments());
                    // .resolve("data.trig")
                    FileSyncImpl fs = FileSyncImpl.create(absPath, !allowEmptyGraphs);

                    return new SyncedDataset(fs);
                }
            });
        return result;
    }

    public Txn local() {
        Txn result = txns.get();
        return result;
    }


//    public DatasetGraphFromTxnMgr(boolean useJournal, TxnMgr txnMgr, Collection<DatasetGraphIndexPlugin> indexers) {
//        this(useJournal, txnMgr, indexers, 100);
//    }

//    public DatasetGraphFromTxnMgr(boolean useJournal, TxnMgr txnMgr, boolean autoDeleteEmptyGraphs, Collection<DatasetGraphIndexPlugin> indexers, long maxCacheSize) {
//        this(useJournal, txnMgr, autoDeleteEmptyGraphs, indexers, CacheBuilder.newBuilder().maximumSize(maxCacheSize));
//    }

//	public DatasetGraphFromTxnMgr(TxnMgr txnMgr, Collection<DatasetGraphIndexPlugin> indexers, CacheBuilder<?, ?> cacheBuilder) {
//		this(txnMgr, indexers, cacheBuilder);
//	}

    @SuppressWarnings("unchecked")
    public DatasetGraphFromTxnMgr(
            String dataFileName,
            boolean useJournal,
            TxnMgr txnMgr,
            boolean allowEmptyGraphs,
            boolean isParallel,
            Collection<DatasetGraphIndexPlugin> indexers,
            CacheBuilder<?, ?> cacheBuilder) {
        super();
        this.dataFileName = dataFileName;
        this.useJournal = useJournal;
        this.txnMgr = txnMgr;
        this.indexers = indexers;
        this.allowEmptyGraphs = allowEmptyGraphs;
        this.isParallel = isParallel;
        this.syncCache = createCache(txnMgr, allowEmptyGraphs, (CacheBuilder<org.aksw.commons.path.core.Path<String>, SyncedDataset>)cacheBuilder);

        this.storeBaseSegments = getStoreBaseSegments(txnMgr);
    }


    public static org.aksw.commons.path.core.Path<String> getStoreBaseSegments(TxnMgr txnMgr) {
        String[] segments = PathUtils.getPathSegments(
                txnMgr.getRootPath().relativize(txnMgr.getResRepo().getRootPath()));
        org.aksw.commons.path.core.Path<String> result = PathOpsStr.newRelativePath(segments);

        return result;
    }


    public TxnMgr getTxnMgr() {
        return txnMgr;
    }


    public LoadingCache<org.aksw.commons.path.core.Path<String>, SyncedDataset> getSyncCache() {
        return syncCache;
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public void begin(TxnType type) {
        // TODO We treat READ_PROMOTE as write which is not optimal
        ReadWrite rw = TxnType.READ_PROMOTE.equals(type)
            ? ReadWrite.WRITE
            : TxnType.convert(type);

        begin(rw);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        Txn txn = txns.get();
        if (txn != null) {
            throw new RuntimeException("Already in a transaction");
        }

        boolean isWrite = ReadWrite.WRITE.equals(readWrite);

        try {
            txn = txnMgr.newTxn(useJournal, isWrite);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        txns.set(txn);
    }


    @Override
    public boolean promote(Promote mode) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Commit first syncs any in-memory changes to temporary files.
     * Only if this step succeeds a 'commit' journal entry is created which indicates that
     * persisting of changes succeeded and and is ready to replace existing data.
     *
     *
     */
    @Override
    public void commit() {

        try {
            // TODO Non-write transactions can probably skip the sync block - or?
            try (Stream<org.aksw.commons.path.core.Path<String>> stream = local().streamAccessedResourcePaths()) {
                Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
                while (it.hasNext()) {
                    org.aksw.commons.path.core.Path<String> relPath = it.next();
                    logger.debug("Syncing: " + relPath);
                    // Path relPath = txnMgr.getResRepo().getRelPath(res);

                    TxnResourceApi api = local().getResourceApi(relPath);
                    if (api.getTxnResourceLock().ownsWriteLock()) {
                        // If we own a write lock and the state is dirty then sync
                        // If there are any in memory changes then write them out
                        SyncedDataset synced = syncCache.get(relPath);
                        if (synced != null) {
                             synced.save();
                        }

                        // Precommit: Copy any new data files to their final location (but keep backups)
                        ContentSync fs = api.getFileSync();
                        fs.preCommit();

                        // Update the in memory cache
                        if (synced != null) {
                            synced.updateState();
                        }
//							if (synced.isDirty()) {
        //						synced.getAdditions().clear();
        //						synced.getDeletions().clear();
//							}


                        // The indexers are now run immediately on insert
    //					for (DatasetGraphIndexPlugin indexer : indexers) {
    //						for (Quad quad : SetFromDatasetGraph.wrap(synced.getDeletions())) {
    //							indexer.delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    //						}
    //
    //						for (Quad quad : SetFromDatasetGraph.wrap(synced.getAdditions())) {
    //							indexer.add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    //						}
    //					}
                    }
                }
            }
                // Once all modified graphs are written out
                // add the statement that the commit action can now be run
                local().addCommit();

                applyJournal(local(), syncCache);
        } catch (Exception e) {
            try {
                local().addRollback();
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw new RuntimeException(e2);
            }

            try {
                applyJournal(local(), syncCache);
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        } finally {
            end();
        }
    }

    public static void applyJournal(Txn txn, LoadingCache<org.aksw.commons.path.core.Path<String>, SyncedDataset> syncCache) {
        TxnMgr txnMgr = txn.getTxnMgr();
        // ResourceRepository<String> resRepo = txnMgr.getResRepo();
        Path resRepoRootPath = txnMgr.getRootPath();

        boolean isCommit;
        try {
            isCommit = txn.isCommit() && !txn.isRollback();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        try {

            // Run the finalization actions
            // As these actions remove undo information
            // there is no turning back anymore
            if (isCommit) {
                txn.addFinalize();
            }

            // TODO Stream the relPaths rather than the string resource names?
            try (Stream<org.aksw.commons.path.core.Path<String>> stream = txn.streamAccessedResourcePaths()) {
                Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
                while (it.hasNext()) {
                    org.aksw.commons.path.core.Path<String> res = it.next();
                    logger.debug("Finalizing and unlocking: " + res);
                    TxnResourceApi api = txn.getResourceApi(res);

                    org.aksw.commons.path.core.Path<String> resourceKey = api.getResourceKey();

                    Path targetFile = api.getFileSync().getTargetFile();
                    if (isCommit) {
                        api.finalizeCommit();
                    } else {
                        api.rollback();
                    }

                    // Clean up empty paths
                    FileUtils.deleteEmptyFolders(targetFile.getParent(), resRepoRootPath, true);

                    SyncedDataset synced = syncCache.getIfPresent(resourceKey);
                    if (synced != null) {
                        if (synced.isDirty()) {
                            if (isCommit) {
                                synced.getDiff().materialize();
                            } else {
                                synced.getDiff().clearChanges();
                            }
                            synced.updateState();
                        }
                    }

                    api.unlock();
                    api.undeclareAccess();
                }
            }

            txn.cleanUpTxn();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void abort() {
        try {
            local().addRollback();
            applyJournal(local(), syncCache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            end();
        }
    }

    @Override
    public void end() {

        // TODO Apply the changes
        // local().applyChanges();

        // Iterate all resources and remove any locks
//		try {
//			local().streamAccessedResources().forEach(r -> {
//				local().getResourceApi(r).unlock();
//			});
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}

//		ResourceApi api = local().getResourceApi(iri);
//		api.lock(true);

        txns.remove();
    }

    @Override
    public ReadWrite transactionMode() {
        boolean isWrite = local().isWrite();
        ReadWrite result = isWrite ? ReadWrite.WRITE : ReadWrite.READ;
        return result;
    }

    @Override
    public TxnType transactionType() {
        ReadWrite rw = transactionMode();
        TxnType result = TxnType.convert(rw);
        return result;
    }

    @Override
    public boolean isInTransaction() {
        boolean result = local() != null;
        return result;
    }


    protected void acquireResourceLock(Txn txn, TxnResourceApi api) throws IOException {
        // FIXME If the lock cannot be acquired check for deadlocks and stale txns
        try {
            api.lock(txn.isWrite());
        } catch (Exception e) {

            // Cancel any stale txns
            cleanupStaleTxns();

            // If after the clean up this txn is part of a cycle then abort it
            Set<GraphPath<Node, Triple>> cycles = TxnUtils.detectDeadLocksRaw(txnMgr);
            Set<String> txnIds = TxnUtils.graphPathsToTxnIds(cycles);

            String txnId = txn.getId();

            if (txnIds.contains(txnId)) {
                rollbackOrEnd(txn);
            }

        }
    }


    public DatasetGraph mapToDatasetGraph(Txn local, TxnResourceApi api) {
        api.declareAccess();

        // api.lock(local.isWrite());
        try {
            acquireResourceLock(local, api);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//		Txn txn = local();
//		if (txn != null) {
//			api.lock(txn.isWrite());
//		}

        org.aksw.commons.path.core.Path<String> resourceKey = api.getResourceKey();
        SyncedDataset entry;
        try {
            entry = syncCache.get(resourceKey);
            // entry.updateIfNeeded();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return entry.get();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        Iterator<Node> result = access(this, () -> {
            Txn local = local();


            try (Stream<TxnResourceApi> stream = local().listVisibleFiles(storeBaseSegments)) {

//                dgStream = Flowable.fromStream(baseStream)
//                        .compose(RxOps.createParallelMapperOrdered(resourceTxnApi -> {
//                            DatasetGraph r = mapToDatasetGraph(local, resourceTxnApi);
//                            return r;
//                        }))
//                        .blockingStream();

                return mapStreamToDatasetGraph(isParallel, local, stream)
                    // .map(api -> mapToDatasetGraph(local, api))
                    .collect(Collectors.toList()).stream() // FIXME only collect if not in a txn
                    .flatMap(dataset -> {
                        return Streams.stream(dataset.listGraphNodes());
                    }).iterator();
            }
        });

        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createNamedGraph(this, Quad.defaultGraphIRI);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        mutateGraph(graphName, dg -> {
            dg.getAddedGraphs().add(graphName);
            dg.getRemovedGraphs().remove(graphName);
            return true;
        });
    }

    @Override
    public void removeGraph(Node graphName) {
        // delete(graphName, Node.ANY, Node.ANY, Node.ANY);

        mutateGraph(graphName, dg -> {
            // Clear the graph; later dataset changes may add triples again
            Graph g = dg.getGraph(graphName);
            g.clear();

            dg.getAddedGraphs().remove(graphName);
            dg.getRemovedGraphs().add(graphName);

            return true;
        });

        // throw new UnsupportedOperationException("not implemented yet");
//		String iri = graphName.getURI();
//		local().getResourceApi(null)
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        boolean result = access(this, () -> super.contains(g, s, p, o));
        return result;
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
//		System.out.println(new Quad(g, s, p, o));
        mutateGraph(g, dg -> {
            boolean r = !dg.contains(g, s, p, o);
            if (r) {
                Txn txn = local();

                dg.add(g, s, p, o);
                for (DatasetGraphIndexPlugin indexer : indexers) {
                    indexer.add(txn, dg, g, s, p, o);
                }

                // Ensure the graph is no longer declared as removed
                dg.getRemovedGraphs().remove(g);
            }


            return r;
        });

//		mutate(this, () -> {
//			String iri = g.getURI();
//			Path relPath = txnMgr.getResRepo().getRelPath(iri);
//
//			// Get the resource and lock it for writing
//			// The lock is held until the end of the transaction
//			ResourceApi api = local().getResourceApi(iri);
//			api.declareAccess();
//			api.lock(true);
//
//			Synced<?, DatasetGraph> synced;
//			try {
//				synced = syncCache.get(relPath);
//			} catch (ExecutionException e) {
//				throw new RuntimeException(e);
//			}
//			DatasetGraph dg = synced.get();
//
//			if (!dg.contains(g, s, p, o)) {
//				synced.setDirty(true);
//				dg.add(g, s, p, o);
//			}
//		});
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        mutateGraph(g, dg -> {
            Graph graph = dg.getGraph(g);

            boolean r = graph.contains(s, p, o);
            if (r) {
                Txn txn = local();

                for (DatasetGraphIndexPlugin indexer : indexers) {
                    indexer.delete(txn, dg, g, s, p, o);
                }
                // dg.delete(g, s, p, o);
                graph.delete(s, p, o);

                if (!allowEmptyGraphs) {
//                    boolean isEmptyGraph = graph.isEmpty();
                  boolean isEmptyGraph = isEmpty(graph);
                    if (isEmptyGraph) {
                        dg.getRemovedGraphs().add(g);
                    }

                }

            }
            return r;
        });
    }

    // GraphView's isEmpty method is not implemented efficiently - it uses size() which iterates all triples
    public static boolean isEmpty(Graph graph) {
        boolean result;
        ExtendedIterator<Triple> it = graph.find();
        try {
            result = !it.hasNext();
        } finally {
            it.close();
        }
        return result;
    }

    protected org.aksw.commons.path.core.Path<String> getResourceKey(String iri) {
        Path path = PathUtils
                .resolve(txnMgr.getResRepo().getRootPath(), txnMgr.getResRepo().getPathSegments(iri))
                .resolve(dataFileName);

        org.aksw.commons.path.core.Path<String> result = pathToKey(path);
        return result;
    }

//    String[] key = PathUtils.getPathSegments(
//            txnMgr.getRootPath().relativize(txnMgr.getResRepo().getRootPath()));
//
//    key = ArrayUtils.addAll(key, txnMgr.getResRepo().getPathSegments(iri));
//    key = ArrayUtils.add(key, "data.trig");
//    return key;
    // key = ArrayUtils.addAll(key, txnMgr.getResRepo().getPathSegments(iri));
//    key = ArrayUtils.add(key, "data.trig");
//    return key;

    protected org.aksw.commons.path.core.Path<String> pathToKey(Path path) {
        String[] keyRaw = PathUtils.getPathSegments(
                txnMgr.getRootPath().relativize(path));

        org.aksw.commons.path.core.Path<String> key = PathOpsStr.newRelativePath(keyRaw);
        return key;
    }


    /**
     *
     * @param graphNode
     * @param mutator A predicate with side effect; true means a change was performed
     */
    protected void mutateGraph(Node graphNode, Predicate<DatasetGraphDiff> mutator) {
        mutate(this, () -> {
            String iri = graphNode.getURI();
            // String[] key = txnMgr.getResRepo().getPathSegments(iri);
            org.aksw.commons.path.core.Path<String> key = getResourceKey(iri);

            // Path relPath = FileUtilsX.resolve(txnMgr.getResRepo().getRootPath(), key);

            // Get the resource and lock it for writing
            // The lock is held until the end of the transaction
            TxnResourceApi api = local().getResourceApi(key); //iri);
            api.declareAccess();

            try {
                acquireResourceLock(local(), api);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // api.lock(true);

            SyncedDataset synced;
            try {
                synced = syncCache.get(key);
                // synced.updateIfNeeded();

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            DatasetGraphDiff dg = synced.get();
            org.apache.jena.system.Txn.executeWrite(dg, () -> mutator.test(dg));

            // boolean isDirty = mutator.test(dg);
//			if (isDirty) {
//				synced.setDirty(true);
//			}
        });
    }

    /**
     * Copied from {@link DatasetGraphWrapper}
     *
     * @param <T>
     * @param mutator
     * @param payload
     */
    public static <T> void mutate(Transactional txn, Runnable mutator) {
        if (txn.isInTransaction()) {
            if (!txn.transactionMode().equals(ReadWrite.WRITE)) {
                TxnType mode = txn.transactionType();
                switch (mode) {
                case WRITE:
                    break;
                case READ:
                    throw new JenaTransactionException("Tried to write inside a READ transaction!");
                case READ_COMMITTED_PROMOTE:
                case READ_PROMOTE:
                    throw new RuntimeException("promotion not implemented");
//                    boolean readCommitted = (mode == TxnType.READ_COMMITTED_PROMOTE);
//                    promote(readCommitted);
                    //break;
                }
            }

            mutator.run();
        } else {
            org.apache.jena.system.Txn.executeWrite(txn, () -> {
                mutator.run();
            });
        }
    }





    @Override
    public void add(Quad quad)
    { add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

    @Override
    public void delete(Quad quad)
    { delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

//    @Override
//    public void add(Node g, Node s, Node p, Node o) {
//        mutate(x -> {
//            if (!contains(g, s, p, o)) {
//                indexPlugins.forEach(plugin -> plugin.add(g, s, p, o));
//                getW().add(g, s, p, o);
//            }
//        }, null);
//    }
//
//
//    @Override
//    public void delete(Node g, Node s, Node p, Node o) {
//        mutate(x -> {
//            if (contains(g, s, p, o)) {
//                indexPlugins.forEach(plugin -> plugin.delete(g, s, p, o));
//                getW().delete(g, s, p, o);
//            }
//        }, null);
//    }

//    @Override
//    public void deleteAny(Node g, Node s, Node p, Node o)
//    { mutate(x -> getW().deleteAny(g, s, p, o), null); }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        super.deleteAny(g, s, p, o);
    }

    public static <T> T access(Transactional txn, Supplier<T> source) {
        return txn.isInTransaction() ? source.get() : org.apache.jena.system.Txn.calculateRead(txn, source::get);
    }


    /**
     * Accessing an iterator outside of a transaction creates an ad-hoc internal
     * txn in which all items are materialized. A warning is logged if that
     * set of items is large.
     *
     */
    public static <T> Iterator<T> accessIterator(Transactional txn, Supplier<? extends Iterator<T>> source) {

        Iterator<T> result;
        if (txn.isInTransaction()) {
            result = source.get();
        } else {
            // Materialize the iterator within an ad-hoc transaction
            // Raises a warning upon accessing too many items
            result = org.apache.jena.system.Txn.calculateRead(txn, () -> {
                List<T> materialized = Lists.newArrayList(source.get());
                if (materialized.size() > 100) {
                    Exception warning = new RuntimeException(String.format("Many items seen in ad-hoc txn (thread %s) - consider managing the txn explicitly", Thread.currentThread().getName()));
                    logger.warn("", warning);
                }
                return materialized.iterator();
            });
        }

        return result;
    }

//
//
//    // @Override
//    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
//    	return local().listVisibleFiles().flatMap(api -> {
//    		Path path = api.getResFilePath();
//    		Synced<?, DatasetGraph> entry = syncCache.get(path);
//    		DatasetGraph dg = entry.get();
//    		Stream<Quad> r = Streams.stream(dg.find(g, s, p, o));
//    		return r;
//    	});
//    }

//        DatasetGraphIndexPlugin bestPlugin = findBestMatch(
//                indexPlugins.iterator(), plugin -> plugin.evaluateFind(s, p, o), (lhs, rhs) -> lhs != null && lhs < rhs);
//
//        Iterator<Node> gnames = bestPlugin != null
//            ? bestPlugin.listGraphNodes(s, p, o)
//            : listGraphNodes();
//
//        IteratorConcat<Quad> iter = new IteratorConcat<>() ;
//
//        // Named graphs
//        for ( ; gnames.hasNext() ; )
//        {
//            Node gn = gnames.next();
//            Iterator<Quad> qIter = findInSpecificNamedGraph(gn, s, p, o) ;
//            if ( qIter != null )
//                iter.add(qIter) ;
//        }
//        return iter ;


    protected Stream<Quad> findInSpecificNamedGraph(Txn local, Node g, Node s, Node p , Node o) {
        logger.debug("Find in specific named graph: " + QuadUtils.create(Arrays.asList(g, s, p, o).stream().map(NodeUtils::nullToAny).collect(Collectors.toList()).toArray(new Node[0])));
        String graphName = g.getURI();
        // String[] relPath = txnMgr.getResRepo().getPathSegments(res);
        org.aksw.commons.path.core.Path<String> relPath = getResourceKey(graphName);

        return Stream.of(local.getResourceApi(relPath))
                .filter(TxnResourceApi::isVisible)
                .map(api -> mapToDatasetGraph(local, api))
                    // .collect(Collectors.toList()).stream() // FIXME only collect if not in a txn

                // TODO We may want to allow relativizing 'g' for lookups accross paths
                .flatMap(dg -> Streams.stream(dg.find(g, s, p, o)));

//    	return access(this, () -> Stream.of(local().getResourceApi(relPath))
//        	.filter(TxnResourceApi::isVisible)
//			.map(this::mapToDatasetGraph)
//				// .collect(Collectors.toList()).stream() // FIXME only collect if not in a txn
//			.flatMap(dg -> Streams.stream(dg.find(Node.ANY, s, p, o))));
    }


    public Stream<TxnResourceApi> findResources(Txn txn, Node s, Node p, Node o) {
        DatasetGraphIndexPlugin bestPlugin = findBestMatch(
                indexers.iterator(),
                plugin -> plugin.evaluateFind(s, p, o), (lhs, rhs) -> lhs != null && lhs < rhs);

        Stream<TxnResourceApi> visibleMatchingResources = bestPlugin != null
                ? bestPlugin.listGraphNodes(txn, this, s, p, o)
                    .map(relPath -> txn.getResourceApi(relPath))
                    .filter(TxnResourceApi::isVisible)
                : local().listVisibleFiles(storeBaseSegments);

        return visibleMatchingResources;
    }


    protected static <I, O> Stream<O> mapStream(
            boolean isParallel,
            Stream<I> baseStream,
            Function<? super I, O> mapper) {
        Stream<O> dgStream;
        if (isParallel) {
            dgStream = Flowable.fromStream(baseStream)
                .compose(RxOps.createParallelMapperOrdered(mapper))
                .blockingStream();
        } else {
            dgStream = baseStream.map(mapper);
        }

        return dgStream;
    }


    public Stream<DatasetGraph> mapStreamToDatasetGraph(boolean isParallel, Txn local, Stream<TxnResourceApi> baseStream) {
        return mapStream(isParallel, baseStream, resourceTxnApi -> mapToDatasetGraph(local, resourceTxnApi));
    }

    public Stream<Quad> findInAnyNamedGraphsCore(Txn local, Node s, Node p, Node o) {
        // findResources(s, p, o)
        Stream<TxnResourceApi> baseStream = findResources(local, s, p, o);


        Stream<Quad> result = mapStreamToDatasetGraph(isParallel, local, baseStream)
                // .collect(Collectors.toList()).stream() // FIXME only collect if not in a txn
                .flatMap(dg -> {
                    return Streams.stream(dg.find(Node.ANY, s, p, o));
                });

        return result;
    }



    public Stream<Quad> findInAnyNamedGraphs(Txn local, Node s, Node p, Node o) {
        logger.debug("Find in any named graph: " + Triple.createMatch(s, p, o));

        // TODO Link the stream to the txn so at latest upon ending the txn the resource can be freed
        // return access(this, () -> findInAnyNamedGraphsCore(s, p, o));

        return findInAnyNamedGraphsCore(local, s, p, o);
                // return stream.collect(Collectors.toList()).iterator();
    }

    public static <T> ClosableIterator<T> streamToClosableIterator(Stream<T> stream) {
        return WrappedIterator.create(Iter.onClose(stream.iterator(), () -> stream.close()));
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {

        return accessIterator(this, () -> {
            Txn local = local();
            Stream<Quad> stream = g == null || Node.ANY.equals(g)
                ? findInAnyNamedGraphs(local, s, p, o)
                : findInSpecificNamedGraph(local, g, s, p, o);


            Iterator<Quad> r = streamToClosableIterator(stream);
            return r;
        });
//    	return Txn.calculateRead(this, () -> local().listVisibleFiles().flatMap(api -> {
//		Path path = api.getResFilePath();
//		SyncedDataset entry;
//		try {
//			entry = syncCache.get(path);
//		} catch (ExecutionException e) {
//			throw new RuntimeException(e);
//		}
//		DatasetGraph dg = entry.get();
//		Stream<Quad> r = Streams.stream(dg.find(g, s, p, o));
//		return r;
//	}).iterator());
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return find(g, s, p, o);
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }


    public void cleanupStaleTxns() throws IOException {
        logger.info("Checking existing txns...");
        try (Stream<Txn> stream = txnMgr.streamTxns()) {
            stream.forEach(txn -> {
                try {
                    // if (txn.isStale()) {
                    if (txn.claim()) {
                        rollbackOrEnd(txn);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process txn", e);
                }
            });
        }
    }

    public void rollbackOrEnd(Txn txn) throws IOException {
        logger.info("Detected stale txn; applying rollback: " + txn.getId());
        if (!txn.isCommit()) {
            txn.addRollback();
        }
        DatasetGraphFromTxnMgr.applyJournal(txn, getSyncCache());
    }

    public static <T, S> Entry<T, S> findBestMatchWithScore(
            Iterator<T> it,
            Function<? super T, ? extends S> itemToScore, BiPredicate<? super S, ? super S> isLhsBetternThanRhs) {

        T bestItem = null;
        S bestScore = null;

        while (it.hasNext()) {
            T item = it.next();
            S score = itemToScore.apply(item);
            if (score != null) {
                if (bestScore == null || isLhsBetternThanRhs.test(score, bestScore)) {
                    bestItem = item;
                    bestScore = score;
                }
            }
        }

        Entry<T, S> result = bestItem == null
                ? null
                : new SimpleEntry<>(bestItem, bestScore);
        return result;
    }

    public static <T, S> T findBestMatch(
            Iterator<T> it,
            Function<? super T, ? extends S> itemToScore, BiPredicate<? super S, ? super S> isLhsBetternThanRhs) {

        Entry<T, S> tmp = findBestMatchWithScore(it, itemToScore, isLhsBetternThanRhs);
        T result = tmp == null ? null : tmp.getKey();
        return result;
    }
}
