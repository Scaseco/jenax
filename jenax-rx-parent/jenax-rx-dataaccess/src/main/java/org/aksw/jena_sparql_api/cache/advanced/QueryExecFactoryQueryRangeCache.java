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
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactories;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.aksw.jenax.sparql.query.rx.ResultSetRx;
import org.aksw.jenax.sparql.query.rx.ResultSetRxImpl;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

/**
 *
 * Does not optimize / normalize prefixes.
 *
 * @author raven
 *
 */
public class QueryExecFactoryQueryRangeCache extends QueryExecFactoryQueryWrapperBase<QueryExecFactoryQuery> {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecFactoryQueryRangeCache.class);

    protected ObjectStore objectStore;
    protected AdvancedRangeCacheConfig cacheConfig;
    protected ArrayOps<Binding[]> arrayOps = ArrayOps.createFor(Binding.class);

    protected Cache<Path<String>, ListPaginator<Binding>> queryToCache;

    public QueryExecFactoryQueryRangeCache(QueryExecFactoryQuery decoratee, ObjectStore objectStore,
            int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        super(decoratee);
        this.objectStore = objectStore;
        this.cacheConfig = cacheConfig;
        this.queryToCache = CacheBuilder.newBuilder().maximumSize(maxCachedQueries).build();
    }

    @Override
    public QueryExec create(Query query) {
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
                ListPaginator<Binding> backend = new ListPaginatorSparql(bodyQueryWithoutSlice, decoratee);
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

                ListPaginatorWithAdvancedCache<Binding> r = ListPaginatorWithAdvancedCache.create(backend,
                        cacheBuilder);

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

    public static QueryExecFactoryQueryRangeCache create(QueryExecFactoryQuery decoratee,
            java.nio.file.Path cacheFolder, int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {
        KryoPool kryoPool = KryoUtils.createKryoPool(JenaKryoRegistratorLib::registerClasses);
        ObjectStore objectStore = ObjectStoreImpl.create(cacheFolder, ObjectSerializerKryo.create(kryoPool));
        QueryExecFactoryQueryRangeCache result = new QueryExecFactoryQueryRangeCache(decoratee, objectStore,
                maxCachedQueries, cacheConfig);
        return result;
    }

    public static QueryExecFactoryQueryRangeCache create(QueryExecFactoryQuery decoratee,
            java.nio.file.Path cacheFolder, long maxRequestSize) {
        AdvancedRangeCacheConfig arcc = AdvancedRangeCacheConfigImpl.newDefaultsForObjects(maxRequestSize);
        QueryExecFactoryQueryRangeCache result = create(decoratee, cacheFolder, 1024, arcc);

        return result;
    }

    public static QueryExecFactoryQueryTransform createQueryExecMod(java.nio.file.Path cacheDir, long maxRequestSize) {
        return qef -> create(qef, cacheDir, maxRequestSize);
    }

    public static LinkSparqlQueryTransform createLinkMod(java.nio.file.Path cacheDir, long maxRequestSize) {
        return link -> {
            QueryExecFactoryQuery qef = QueryExecFactories.of(link);
            qef = create(qef, cacheDir, maxRequestSize);
            LinkSparqlQuery result = QueryExecFactories.toLink(qef);
            return result;
        };
    }

    public static LinkSparqlQuery decorate(LinkSparqlQuery link, java.nio.file.Path cacheDir, long maxRequestSize) {
        LinkSparqlQueryTransform mod = createLinkMod(cacheDir, maxRequestSize);
        LinkSparqlQuery result = mod.apply(link);
        return result;
    }
}
