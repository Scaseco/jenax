package org.aksw.jena_sparql_api.cache.advanced;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.slice.SliceWithPagesSyncToDisk;
import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.cache.range.ListPaginatorWithAdvancedCache;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.KryoUtils;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.jena_sparql_api.lookup.ListPaginatorSparql;
import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryDecorator;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.aksw.jenax.sparql.query.rx.ResultSetRx;
import org.aksw.jenax.sparql.query.rx.ResultSetRxImpl;
import org.apache.jena.ext.com.google.common.io.BaseEncoding;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import io.reactivex.rxjava3.core.Flowable;

public class QueryExecutionFactoryRangeCache
    extends QueryExecutionFactoryDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionFactoryRangeCache.class);

    protected ObjectStore objectStore;
    protected AdvancedRangeCacheConfig cacheConfig;
    protected ArrayOps<Binding[]> arrayOps = ArrayOps.createFor(Binding.class);

    protected Cache<String, ListPaginator<Binding>> queryToCache;


    public QueryExecutionFactoryRangeCache(
            QueryExecutionFactory decoratee,
            ObjectStore objectStore,
            int maxCachedQueries,
            AdvancedRangeCacheConfig cacheConfig
            ) {
        super(decoratee);
        this.objectStore = objectStore;
        this.cacheConfig = cacheConfig;
        this.queryToCache = CacheBuilder.newBuilder()
                .maximumSize(maxCachedQueries)
                .build();
    }

    public static QueryExecutionFactoryRangeCache create(
            QueryExecutionFactory decoratee, java.nio.file.Path baseFolder, int maxCachedQueries, AdvancedRangeCacheConfig cacheConfig) {

        ObjectStore objectStore = ObjectStoreImpl.create(baseFolder, ObjectSerializerKryo.create(
                KryoUtils.createKryoPool(JenaKryoRegistratorLib::registerClasses)));

        QueryExecutionFactoryRangeCache result = new QueryExecutionFactoryRangeCache(decoratee, objectStore, maxCachedQueries, cacheConfig);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Range<Long> requestRange = QueryUtils.toRange(query);

        Query queryWithoutSlice = query.cloneQuery();
        queryWithoutSlice.setOffset(Query.NOLIMIT);
        queryWithoutSlice.setLimit(Query.NOLIMIT);

        HashCode hashCode = Hashing.sha256().hashString(queryWithoutSlice.toString(), StandardCharsets.UTF_8);
        String str = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        logger.debug("Query w/o slice: " + queryWithoutSlice);
        logger.debug("Query hash: " + str + " " + requestRange);

        List<Var> resultVars = queryWithoutSlice.getProjectVars();

        ListPaginator<Binding> frontend;
        try {
            frontend = queryToCache.get(str, () -> {
                ListPaginator<Binding> backend = new ListPaginatorSparql(queryWithoutSlice, decoratee::createQueryExecution);
                // SequentialReaderSource<Binding[]> dataSource = SequentialReaderSourceRx.create(arrayOps, backend);

                // KeyObjectStore store = KeyObjectStoreWithKeyTransform.wrapWithPrefix(objectStore, List.of(str));
                Path<String> objectStorePath = PathOpsStr.newRelativePath(str);
                SliceWithPagesSyncToDisk<Binding[]> sliceBuffer = SliceWithPagesSyncToDisk.create(
                        arrayOps, objectStore, objectStorePath,
                        cacheConfig.getPageSize(), cacheConfig.getTerminationDelay());


                AdvancedRangeCacheImpl.Builder<Binding[]> cacheBuilder = AdvancedRangeCacheImpl.<Binding[]>newBuilder()
                        // .setDataSource(dataSource)
                        .setWorkerBulkSize(128)
                        .setSlice(sliceBuffer)
                        .setRequestLimit(cacheConfig.getMaxRequestSize())
                        .setTerminationDelay(cacheConfig.getTerminationDelay());

                ListPaginatorWithAdvancedCache<Binding> r = ListPaginatorWithAdvancedCache.create(backend, cacheBuilder);


//                SmartRangeCacheImpl<Binding> r = SmartRangeCacheImpl.wrap(
//                        backend, store, 10000, 10, Duration.ofSeconds(1), 10000, 10000);

                logger.debug("Is cache complete? " + r.getCore().getSlice().isComplete());

                return r;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }




        Flowable<Binding> flowable = frontend.apply(requestRange);
        ResultSetRx rs = ResultSetRxImpl.create(resultVars, flowable);
        QueryExecution result = rs.asQueryExecution();

        return result;
    }
}
