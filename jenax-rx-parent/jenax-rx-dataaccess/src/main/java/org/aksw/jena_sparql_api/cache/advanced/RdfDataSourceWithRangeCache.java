package org.aksw.jena_sparql_api.cache.advanced;


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
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.update.UpdateExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateWrapperBase;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.aksw.jenax.sparql.query.rx.ResultSetRx;
import org.aksw.jenax.sparql.query.rx.ResultSetRxImpl;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

/**
 * This class wraps a data source with caching.
 * The wrapping includes an additional MRSW lock (multiple reads or single writer).
 */
public class RdfDataSourceWithRangeCache
    extends RdfDataSourceWrapperBase
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecFactoryQueryRangeCache.class);

    // protected Transactional transactional = TransactionalLock.createMRSW();

    protected ObjectStore objectStore;
    protected AdvancedRangeCacheConfig cacheConfig;
    protected ArrayOps<Binding[]> arrayOps = ArrayOps.createFor(Binding.class);
    protected Cache<Path<String>, ListPaginator<Binding>> queryToCache;

    public RdfDataSourceWithRangeCache(RdfDataSource delegate, ObjectStore objectStore,
            int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        super(delegate);
        this.objectStore = objectStore;
        this.cacheConfig = cacheConfig;
        this.queryToCache = CacheBuilder.newBuilder().maximumSize(maxCachedQueries).build();
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection baseConn = getDelegate().getConnection();

        LinkSparqlUpdateTransform updateXform = baseLink -> new LinkSparqlUpdateWrapperBase(baseLink) {
            @Override
            public UpdateExecBuilder newUpdate() {
                return new UpdateExecBuilderWrapperBase(getDelegate().newUpdate()) {
                    @Override
                    public UpdateExec build() {
                        // Hash the update request
                        return new UpdateExecWrapperBase<>(getDelegate().build());
                    }
                };
            }
        };

//        RDFLink baseLink = RDFLinkAdapter.adapt(baseConn);
//        LinkSparqlQuery baseLinkQuery = RDFLinkUtils.unwrapLinkSparqlQuery(baseLink);
//        Context cxt = baseLinkQuery.newQuery().getContext();

        LinkSparqlQuery lsq = new LinkSparqlQueryBase() {
            @Override
            public QueryExecBuilder newQuery() {
                // XXX Ideally the context would expose a possible underlying dataset's context
                return new QueryExecBuilderCustomBase<>(new Context()) {
                    @Override
                    public QueryExec build() {
                        Query q = getParsedQuery();
                        QueryExec r = execQuery(q);
                        return r;
                    }
                };
            }

            @Override
            public Transactional getDelegate() {
                return baseConn;
            }

            @Override
            public void close() {
                baseConn.close();
                // XXX Should closing a connection close all associated query execs?
            }
        };

        RDFLink link = new RDFLinkModular(lsq, RDFLinkAdapter.adapt(baseConn), RDFLinkAdapter.adapt(baseConn));
        RDFConnection r = RDFConnectionAdapter.adapt(link);

        // RDFConnection r = //RDFConnectionUtils.wrapWithLinkDecorator(baseConn, link -> RDFLinkUtils.apply(link, linkXform));
        return r;
    }


    // @Override
    public QueryExec execQuery(Query query) {
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

        logger.debug("Query w/o slice: " + bodyQueryWithoutSlice);
        logger.debug("Query hash: " + hash + " " + requestRange);

        List<Var> resultVars = query.getProjectVars();

        ListPaginator<Binding> frontend;
        try {
            // The query hash may contain slashes which are parsed into path segments
            Path<String> objectStorePath = PathStr.parse(queryHash);
            frontend = queryToCache.get(objectStorePath, () -> {
                ListPaginator<Binding> backend = new ListPaginatorSparql(bodyQueryWithoutSlice, getDelegate().asQef());
                // SequentialReaderSource<Binding[]> dataSource =
                // SequentialReaderSourceRx.create(arrayOps, backend);

                // KeyObjectStore store =
                // KeyObjectStoreWithKeyTransform.wrapWithPrefix(objectStore, List.of(str));
                SliceWithPagesSyncToDisk<Binding[]> sliceBuffer = SliceWithPagesSyncToDisk.create(arrayOps, objectStore,
                        objectStorePath, cacheConfig.getPageSize(), cacheConfig.getTerminationDelay());

                AdvancedRangeCacheImpl.Builder<Binding[]> cacheBuilder = AdvancedRangeCacheImpl.<Binding[]>newBuilder()
                        // .setDataSource(dataSource)
                        .setWorkerBulkSize(128).setSlice(sliceBuffer).setRequestLimit(cacheConfig.getMaxRequestSize())
                        .setTerminationDelay(cacheConfig.getTerminationDelay());

                ListPaginatorWithAdvancedCache<Binding> r = ListPaginatorWithAdvancedCache.create(backend, cacheBuilder);

                logger.debug("Is cache complete? " + r.getCore().getSlice().isComplete());

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
        QueryExec result = rs.asQueryExec();

        return result;
    }

    public static RdfDataSourceWithRangeCache create(RdfDataSource dataSource,
            java.nio.file.Path cacheFolder, int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        KryoPool kryoPool = KryoUtils.createKryoPool(JenaKryoRegistratorLib::registerClasses);
        ObjectStore objectStore = ObjectStoreImpl.create(cacheFolder, ObjectSerializerKryo.create(kryoPool));
        RdfDataSourceWithRangeCache result = new RdfDataSourceWithRangeCache(dataSource, objectStore,
                maxCachedQueries, cacheConfig);
        return result;
    }

    public static RdfDataSourceWithRangeCache create(RdfDataSource dataSource,
            java.nio.file.Path cacheFolder, long maxRequestSize) {
        AdvancedRangeCacheConfig arcc = AdvancedRangeCacheConfigImpl.newDefaultsForObjects(maxRequestSize);
        RdfDataSourceWithRangeCache result = create(dataSource, cacheFolder, 1024, arcc);

        return result;
    }
//
    public static RdfDataSourceWithRangeCache create(RdfDataSourceWithRangeCache dataSource, java.nio.file.Path cacheDir, long maxRequestSize) {
        return create(dataSource, cacheDir, maxRequestSize);
    }
//
//    public static LinkSparqlQueryTransform createLinkMod(java.nio.file.Path cacheDir, long maxRequestSize) {
//        return link -> {
//            QueryExecFactoryQuery qef = QueryExecFactories.of(link);
//            qef = create(qef, cacheDir, maxRequestSize);
//            LinkSparqlQuery result = QueryExecFactories.toLink(qef);
//            return result;
//        };
//    }
//
//    public static LinkSparqlQuery decorate(LinkSparqlQuery link, java.nio.file.Path cacheDir, long maxRequestSize) {
//        LinkSparqlQueryTransform mod = createLinkMod(cacheDir, maxRequestSize);
//        LinkSparqlQuery result = mod.apply(link);
//        return result;
//    }




//    public static RdfDataSource withCache(RdfDataSource dataSource, Path cachePath, long maxResultSize) {
//
//
//
//        QueryExecFactoryQueryTransform decorizer = QueryExecFactoryQueryRangeCache
//                .createQueryExecMod(cachePath, cmd.dbMaxResultSize);
//
//        QueryExecutionFactory i = QueryExecutionFactories.of(dataSourceTmp);
//        QueryExecFactory j = QueryExecFactories.adapt(i);
//        QueryExecFactory k = QueryExecFactories.adapt(decorizer.apply(j));
//        QueryExecutionFactory l = QueryExecutionFactories.adapt(k);
//        dataSourceTmp = RdfDataEngines.adapt(l);
//    }
}
