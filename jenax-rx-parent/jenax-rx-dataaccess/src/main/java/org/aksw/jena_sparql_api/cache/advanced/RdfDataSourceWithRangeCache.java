package org.aksw.jena_sparql_api.cache.advanced;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.slice.SliceWithPagesSyncToDisk;
import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathStr;
import org.aksw.commons.rx.cache.range.ListPaginatorWithAdvancedCache;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.KryoUtils;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.jena_sparql_api.lookup.ListPaginatorSparql;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.syntax.QueryHash;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.common.TransactionalMultiplex;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.exec.update.UpdateExecWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.link.dataset.LinkDatasetGraphWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateBase;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.aksw.jenax.sparql.query.rx.ResultSetRx;
import org.aksw.jenax.sparql.query.rx.ResultSetRxImpl;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import io.reactivex.rxjava3.core.Flowable;

/**
 * This class wraps a data source with caching.
 * The wrapping includes an additional MRSW lock (multiple reads or single writer) -
 * so only a single write transaction is allowed at any time.
 */
public class RdfDataSourceWithRangeCache
    extends RdfDataSourceWrapperBase
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecFactoryQueryRangeCache.class);

    protected HashCode datasetHash;

    /** Hash functions for update statements */
    protected static HashFunction stmtHashFn = Hashing.md5();
    // HashFunction aggHashFn = Hashing.murmur3_32_fixed();


    /** This lock takes precedence over any locking mechanism of the delegate data source */
    protected Transactional transactional = TransactionalLock.createMRSW();

    protected ObjectStore objectStore;
    protected AdvancedRangeCacheConfig cacheConfig;
    protected ArrayOps<Binding[]> arrayOps = ArrayOps.createFor(Binding.class);
    protected Cache<Path<String>, ListPaginator<Binding>> queryToCache;



    public RdfDataSourceWithRangeCache(RdfDataSource delegate, ObjectStore objectStore,
            HashCode datasetHash, int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        super(delegate);
        this.objectStore = objectStore;
        this.cacheConfig = cacheConfig;
        this.datasetHash = datasetHash;
        this.queryToCache = CacheBuilder.newBuilder().maximumSize(maxCachedQueries).build();
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection baseConn = getDelegate().getConnection();
        RDFLink baseLink = RDFLinkAdapter.adapt(baseConn);

        Transactional combined = new TransactionalMultiplex<>(transactional, baseLink);

        LinkSparqlQuery linkQuery = new LinkSparqlQueryBase() {
            @Override
            public QueryExecBuilder newQuery() {
                // XXX Ideally the context would expose a possible underlying dataset's context
                return new QueryExecBuilderCustomBase<>(new Context()) {
                    @Override
                    public QueryExec build() {
                        QueryExec r = createQueryExec(this);
                        return r;
                    }
                };
            }
            @Override public Transactional getDelegate() { return transactional; }
            // baseConn.close();
            // XXX Should closing a connection close all associated query execs?
            @Override public void close() { baseLink.close(); }
        };

        LinkSparqlUpdate linkUpdate = new LinkSparqlUpdateBase() {
            @Override
            public UpdateExecBuilder newUpdate() {
                return new UpdateExecBuilderCustomBase<UpdateExecBuilder>(new Context()) {
                    @Override
                    public UpdateExec build() {
                        return createUpdateExec(baseLink, this);
                    }
                };
            }
            @Override public Transactional getDelegate() { return combined; }
            @Override public void close() { baseLink.close(); }
        };

        RDFLink link = new RDFLinkModular(
                linkQuery,
                linkUpdate,
                new LinkDatasetGraphWrapperTxn<>(baseLink, combined));
        RDFConnection r = RDFConnectionAdapter.adapt(link);
        return r;
    }

    public QueryExec createQueryExec(QueryExecBuilderCustomBase<?> builder) {
        Query query = builder.getParsedQuery();
        Range<Long> requestRange = QueryUtils.toRange(query);

        Query bodyQueryWithoutSlice = QueryTransformOps.shallowCopy(query); // hash.getBodyQuery();
        bodyQueryWithoutSlice.setOffset(Query.NOLIMIT);
        bodyQueryWithoutSlice.setLimit(Query.NOLIMIT);

        QueryHash hash = QueryHash.createHash(bodyQueryWithoutSlice);
//        HashCode hashCode = Hashing.sha256().hashString(queryWithoutSlice.toString(), StandardCharsets.UTF_8);
//        String str = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        String queryHash = hash.toString();
//        String bodyHash = hash.getBodyHashStr();
//        String projHash = hash.getProjHashStr();

        if (logger.isDebugEnabled()) {
            logger.debug("Query w/o slice: " + bodyQueryWithoutSlice);
            logger.debug("Query hash: " + hash + " " + requestRange);
        }

        List<Var> resultVars = query.getProjectVars();

        ListPaginator<Binding> frontend;
        try {
            // The query hash may contain slashes which are parsed into path segments
            Path<String> queryHashPath = PathStr.parse(queryHash);
            String datasetHashStr = QueryHash.str(datasetHash);
            Path<String> fullPath = PathStr.newRelativePath(datasetHashStr).resolve(queryHashPath);

            frontend = queryToCache.get(fullPath, () -> {
                ListPaginator<Binding> backend = new ListPaginatorSparql(bodyQueryWithoutSlice, getDelegate().asQef());
                // SequentialReaderSource<Binding[]> dataSource =
                // SequentialReaderSourceRx.create(arrayOps, backend);

                // KeyObjectStore store =
                // KeyObjectStoreWithKeyTransform.wrapWithPrefix(objectStore, List.of(str));
                SliceWithPagesSyncToDisk<Binding[]> sliceBuffer = SliceWithPagesSyncToDisk.create(arrayOps, objectStore,
                        fullPath, cacheConfig.getPageSize(), cacheConfig.getTerminationDelay());

                AdvancedRangeCacheImpl.Builder<Binding[]> cacheBuilder = AdvancedRangeCacheImpl.<Binding[]>newBuilder()
                        // .setDataSource(dataSource)
                        .setWorkerBulkSize(128).setSlice(sliceBuffer).setRequestLimit(cacheConfig.getMaxRequestSize())
                        .setTerminationDelay(cacheConfig.getTerminationDelay());

                ListPaginatorWithAdvancedCache<Binding> r = ListPaginatorWithAdvancedCache.create(backend, cacheBuilder);

                if (logger.isDebugEnabled()) {
                    logger.debug("Is cache complete? " + r.getCore().getSlice().isComplete());
                }

//                SmartRangeCacheImpl<Binding> r = SmartRangeCacheImpl.wrap(
//                        backend, store, 10000, 10, Duration.ofSeconds(1), 10000, 10000);

                return r;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // TODO Avoid needless projections
        ListPaginator<Binding> tmp = frontend.map(b -> BindingUtils.project(b, resultVars));
        Flowable<Binding> flowable = tmp.apply(requestRange);
        ResultSetRx rs = ResultSetRxImpl.create(query, resultVars, flowable);
        // QueryExec result = rs.asQueryExec();
        QueryExec result = QueryExecWrapperTxn.wrap(rs.asQueryExec(), transactional);

        return result;
    }


    public UpdateExec createUpdateExec(LinkSparqlUpdate baseLink, UpdateExecBuilderCustomBase<?> builder) {
        UpdateExec core = new UpdateExec() {
            @Override
            public void execute() {
                UpdateExecBuilder baseUpdateExecBuilder = baseLink.newUpdate();
                builder.applySettings(baseUpdateExecBuilder);

                UpdateRequest updateRequest = builder.getParsedUpdateRequest();
                String urStr = updateRequest.toString();

                baseUpdateExecBuilder.execute();

                // TODO For LOAD statements we might want to compute hashes over the referenced content

                HashCode hashCode = stmtHashFn.hashString(urStr, StandardCharsets.UTF_8);
                HashCode newDatasetHash = Hashing.combineOrdered(Arrays.asList(datasetHash, hashCode));

                if (logger.isInfoEnabled()) {
                    logger.info("New dataset id after update: " + QueryHash.str(newDatasetHash));
                }

                datasetHash = newDatasetHash;
            }
        };

        UpdateExec result = UpdateExecWrapperTxn.wrap(core, transactional);
        return result;
    }


    public static RdfDataSourceWithRangeCache create(RdfDataSource dataSource,
            java.nio.file.Path cacheFolder, HashCode datasetHash, int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        KryoPool kryoPool = KryoUtils.createKryoPool(JenaKryoRegistratorLib::registerClasses);
        ObjectStore objectStore = ObjectStoreImpl.create(cacheFolder, ObjectSerializerKryo.create(kryoPool));
        RdfDataSourceWithRangeCache result = new RdfDataSourceWithRangeCache(dataSource, objectStore,
                datasetHash, maxCachedQueries, cacheConfig);
        return result;
    }

    public static RdfDataSourceWithRangeCache create(RdfDataSource dataSource,
            java.nio.file.Path cacheFolder, long maxRequestSize) {
        HashCode hashCode = stmtHashFn.hashInt(0);
        return create(dataSource, cacheFolder, maxRequestSize, hashCode);
    }

    public static RdfDataSourceWithRangeCache create(RdfDataSource dataSource,
            java.nio.file.Path cacheFolder, long maxRequestSize, HashCode datasetHash) {
        AdvancedRangeCacheConfig arcc = AdvancedRangeCacheConfigImpl.newDefaultsForObjects(maxRequestSize);
        RdfDataSourceWithRangeCache result = create(dataSource, cacheFolder, datasetHash, 1024, arcc);

        return result;
    }
//
    public static RdfDataSourceWithRangeCache create(RdfDataSourceWithRangeCache dataSource, java.nio.file.Path cacheDir, long maxRequestSize) {
        return create(dataSource, cacheDir, maxRequestSize);
    }

}

//class LinkSparqlQueryWithRangeCache implements LinkSparqlQueryBase {
//    protected LinkSparqlQuery baseLink;
//    // protected Transactional transactional;
//
//    public LinkSparqlQueryWithRangeCache(LinkSparqlQuery baseLink) { //, Transactional transactional) {
//        this.baseLink = baseLink;
//        // this.transactional = transactional;
//    }
//
//    @Override
//    public QueryExecBuilder newQuery() {
//        // XXX Ideally the context would expose a possible underlying dataset's context
//        return new QueryExecBuilderCustomBase<>(new Context()) {
//            @Override
//            public QueryExec build() {
//                QueryExec r = createQueryExec(this);
//                return r;
//            }
//        };
//    }
//    @Override public Transactional getDelegate() { return transactional; }
//    // baseConn.close();
//    // XXX Should closing a connection close all associated query execs?
//    @Override public void close() { baseLink.close(); }
//}

//    class LinkSparqlUpdateWithRangeCache implements LinkSparqlUpdateBase {
//        protected LinkSparqlUpdate baseLink;
//        protected Transactional transactional;
//
//        public LinkSparqlUpdateWithRangeCache(LinkSparqlUpdate baseLink, Transactional transactional) {
//            this.baseLink = baseLink;
//            this.transactional = transactional;
//        }
//
//        @Override
//        public UpdateExecBuilder newUpdate() {
//            return new UpdateExecBuilderCustomBase<UpdateExecBuilder>(new Context()) {
//                @Override
//                public UpdateExec build() {
//                    return createUpdateExec(baseLink, this);
//                }
//            };
//        }
//        @Override public Transactional getDelegate() { return transactional; }
//        @Override public void close() { baseLink.close(); }
//    }

 // FIXME The linkDatasetGraph API needs to be overridden so that operations adequately update the dataset hash
//         LinkDatasetGraph lgd = new LinkDatasetGraphWrapperBase<>(RDFLinkUtils.unwrapLinkDatasetGraph(baseLink)) {
//             @Override
//             public void load(Node graphName, String file) {
 //
 //
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void load(String file) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void load(Node graphName, Graph graph) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void load(Graph graph) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void put(Node graphName, String file) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void put(String file) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void put(Node graphName, Graph graph) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void put(Graph graph) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void delete(Node graphName) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void delete() {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void loadDataset(String file) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void loadDataset(DatasetGraph dataset) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void putDataset(String file) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void putDataset(DatasetGraph dataset) {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public void clearDataset() {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//             @Override
//             public boolean isClosed() {
//                 // TODO Auto-generated method stub
//                 return false;
//             }
 //
//             @Override
//             public void close() {
//                 // TODO Auto-generated method stub
 //
//             }
 //
//         };


