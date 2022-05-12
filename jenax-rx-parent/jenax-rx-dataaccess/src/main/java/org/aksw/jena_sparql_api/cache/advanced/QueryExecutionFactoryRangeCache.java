package org.aksw.jena_sparql_api.cache.advanced;

import java.util.List;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
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
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListPaginatorSparql;
import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryDecorator;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.util.syntax.QueryHash;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.aksw.jenax.sparql.query.rx.ResultSetRx;
import org.aksw.jenax.sparql.query.rx.ResultSetRxImpl;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class QueryExecutionFactoryRangeCache
    extends QueryExecutionFactoryDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionFactoryRangeCache.class);

    protected ObjectStore objectStore;
    protected AdvancedRangeCacheConfig cacheConfig;
    protected ArrayOps<Binding[]> arrayOps = ArrayOps.createFor(Binding.class);

    protected Cache<Path<String>, ListPaginator<Binding>> queryToCache;


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

        QueryHash hash = QueryHash.createHash(query);

        Query bodyQueryWithoutSlice = hash.getBodyQuery();
        bodyQueryWithoutSlice.setOffset(Query.NOLIMIT);
        bodyQueryWithoutSlice.setLimit(Query.NOLIMIT);

//        HashCode hashCode = Hashing.sha256().hashString(queryWithoutSlice.toString(), StandardCharsets.UTF_8);
//        String str = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        String bodyHash = hash.getBodyHashStr();
        String projHash = hash.getProjHashStr();

        logger.debug("Query w/o slice: " + bodyQueryWithoutSlice);
        logger.debug("Query hash: " + hash + " " + requestRange);

        List<Var> resultVars = bodyQueryWithoutSlice.getProjectVars();

        ListPaginator<Binding> frontend;
        try {
            Path<String> objectStorePath = PathOpsStr.newRelativePath(bodyHash).resolve(projHash);
            frontend = queryToCache.get(objectStorePath, () -> {
                ListPaginator<Binding> backend = new ListPaginatorSparql(bodyQueryWithoutSlice, decoratee::createQueryExecution);
                // SequentialReaderSource<Binding[]> dataSource = SequentialReaderSourceRx.create(arrayOps, backend);

                // KeyObjectStore store = KeyObjectStoreWithKeyTransform.wrapWithPrefix(objectStore, List.of(str));
                SliceWithPagesSyncToDisk<Binding[]> sliceBuffer = SliceWithPagesSyncToDisk.create(
                        arrayOps, objectStore, objectStorePath,
                        cacheConfig.getPageSize(), cacheConfig.getTerminationDelay());


                AdvancedRangeCacheImpl.Builder<Binding[]> cacheBuilder = AdvancedRangeCacheImpl.<Binding[]>newBuilder()
                        // .setDataSource(dataSource)
                        .setWorkerBulkSize(128)
                        .setSlice(sliceBuffer)
                        .setRequestLimit(cacheConfig.getMaxRequestSize())
                        .setTerminationDelay(cacheConfig.getTerminationDelay());

                ListPaginatorWithAdvancedCache<Binding> bodyPaginator = ListPaginatorWithAdvancedCache.create(backend, cacheBuilder);

                logger.debug("Is cache complete? " + bodyPaginator.getCore().getSlice().isComplete());

                ListPaginator<Binding> r = bodyPaginator;




//                SmartRangeCacheImpl<Binding> r = SmartRangeCacheImpl.wrap(
//                        backend, store, 10000, 10, Duration.ofSeconds(1), 10000, 10000);

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


    public static QueryExecutionFactory decorate(QueryExecutionFactory decoratee, java.nio.file.Path cacheDir) {
        KryoPool kryoPool = KryoUtils.createKryoPool(JenaKryoRegistratorLib::registerClasses);

        // KeyObjectStore keyObjectStore = SmartRangeCacheImpl.createKeyObjectStore(Paths.get(cacheDir), kryoPool);
        ObjectStore objectStore = ObjectStoreImpl.create(cacheDir, ObjectSerializerKryo.create(kryoPool));

        AdvancedRangeCacheConfig arcc = AdvancedRangeCacheConfigImpl.newDefaultsForObjects();

//        QueryExecutionFactory core = new QueryExecutionFactoryBackQuery() {
//
//            @Override
//            public QueryExecution createQueryExecution(Query query) {
//                QueryExecution qe = QueryExecutionHTTP.create()
//                        .acceptHeader(ResultSetLang.RS_XML.getContentType().getContentTypeStr())
//                        .endpoint(endpointUrl).query(query).build();
//                return qe;
//            }
//
//            @Override
//            public String getState() {
//                return "";
//            }
//
//            @Override
//            public String getId() {
//                return "";
//            }
//        };

        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(decoratee)
                .config()
//                    .withCache(new CacheBackendMem())
                // .withCache(new CacheBackendFile(new File("/tmp/moinapp/cache"), 1000 * 60 * 60 * 24L))
                .compose(_decoratee -> new QueryExecutionFactoryRangeCache(_decoratee, objectStore, 100, arcc))
//                .withCache(new CacheBackendFile(new File("/tmp/moinapp/cache"), 1000 * 60 * 60 * 24L * 30))
                .end().create();

        return qef;
    }


    public static void main(String[] args) {
    	java.nio.file.Path cacheRoot = java.nio.file.Path.of("/tmp/query-cache");
    	java.nio.file.Path endpointRoot = cacheRoot.resolve("moin/default");

    	QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://moin.aksw.org/sparql")
    			.config()
    			.compose(x -> decorate(x, cacheRoot))
    			.withParser(SparqlQueryParserImpl.create())
    			.end()
    			.create();

    	try (QueryExecution qe = qef.createQueryExecution("SELECT ?p { ?s ?p ?o } LIMIT 10")) {
    		// QueryExecUtils.exec(QueryExec.adapt(qe));
    		ResultSetFormatter.out(System.out, qe.execSelect());
    	}
    }


}
