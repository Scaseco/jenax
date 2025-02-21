package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseIterator;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * RdfDataSource wrapper that uses a simple (= non-streaming) cache both successful and failed executions.
 *
 * The cache keys are of type Entry('queryForm', 'queryObjectOrString').
 * The cache values are either Exception, Table, List<Triple> or List<Quad>.
 *
 * Cancelling a query execution while it is being cached caches the encountered cancellation exception.
 */
public class RdfDataSourceWithSimpleCache
    extends RDFDataSourceWrapperBase<RDFDataSource>
{
    protected Cache<Object, Object> cache;

    public RdfDataSourceWithSimpleCache(RDFDataSource delegate, Cache<Object, Object> cache) {
        super(delegate);
        this.cache = Objects.requireNonNull(cache);
    }

    public Cache<Object, Object> getCache() {
        return cache;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection result = RDFConnectionUtils.wrapWithBuilderTransform(
                super.getConnection(),
                qeb -> new QueryExecBuilderWithSimpleCache(qeb, cache),
                null);
        return result;
    }

    public static class QueryExecBuilderWithSimpleCache
        extends QueryExecBuilderCustomBase<QueryExecBuilder>
    {
        protected QueryExecBuilder delegate;
        protected Cache<Object, Object> cache;

        public QueryExecBuilderWithSimpleCache(QueryExecBuilder delegate, Cache<Object, Object> cache) {
            // super(delegate);
            super();
            this.delegate = delegate;
            this.cache = cache;
        }

        @Override
        public QueryExec build() {
            applySettings(delegate);
            Object queryOrQueryString = query != null ? query : queryString;
            Objects.requireNonNull(queryOrQueryString, "No query or query string was set");

//            if (query != null) {
//                delegate.query(query);
//            } else if (queryString != null) {
//                delegate.query(queryString);
//            }

            return new QueryExecWithSimpleCache(delegate::build, cache, queryOrQueryString);
        }
    }

    public static class QueryExecWithSimpleCache
        extends AutoCloseableWithLeakDetectionBase
        implements QueryExecBaseIterator
    {
        protected Supplier<QueryExec> queryExecSupplier;
        protected Cache<Object, Object> cache;
        protected Object cacheKey;

        // protected final Object lock = new Object();
        protected volatile QueryExec activeExec;
        protected volatile boolean isCancelled;

        public QueryExecWithSimpleCache(Supplier<QueryExec> queryExecSupplier, Cache<Object, Object> cache, Object cacheKey) {
            super();
            this.queryExecSupplier = queryExecSupplier;
            this.cache = cache;
            this.cacheKey = cacheKey;
        }

        @Override
        public void abort() {
            if (!isCancelled) {
                synchronized (this) {
                    if (!isCancelled) {
                        isCancelled = true;
                        if (activeExec != null) {
                            activeExec.abort();
                        }
                    }
                }
            }
        }

        protected QueryExec buildExec() {
            synchronized (this) {
                ensureOpen();

                if (activeExec != null) {
                    throw new RuntimeException("QueryExec already started");
                }

                if (isCancelled) {
                    throw new QueryCancelledException();
                }

                activeExec = queryExecSupplier.get();
            }
            return activeExec;
        }

        public Object getCacheKey() {
            return cacheKey;
        }

        public Cache<Object, Object> getCache() {
            return cache;
        }

        @SuppressWarnings("unchecked")
        protected <T> T get(String category, Function<QueryExec, T> queryExecAccessor) {
            Entry<String, Object> finalKey = Map.entry(category, cacheKey);
            Object tmp = cache.get(finalKey, k -> {
                Object r;
                try (QueryExec qe = buildExec()) {
                    try {
                        r = queryExecAccessor.apply(qe);
                    } catch (Exception e) {
                        // Track as a suppressed exception that control of flow passed through here
                        if (e instanceof RuntimeException) {
                            e.addSuppressed(new RuntimeException("Query execution error"));
                        }
                        r = e;
                    }
                } catch (Exception e) {
                    // Track as a suppressed exception that control of flow passed through here
                    if (e instanceof RuntimeException) {
                        e.addSuppressed(new RuntimeException("Query execution error"));
                    }
                    r = e;
                }
                return r;
            });

            if (tmp instanceof RuntimeException re) {
                throw re;
            } else if (tmp instanceof Exception e) {
                throw new RuntimeException(e);
            }

            return (T)tmp;
        }

        @Override
        public RowSet select() {
            Table table = get("select", qe -> TableUtils.createTable(qe.select()));
            return table.toRowSet();
        }

        @Override
        public Iterator<Triple> constructTriples() {
            List<Triple> list = get("constructTriples", qe -> Iter.asStream(qe.constructTriples()).collect(Collectors.toList()));
            return list.iterator();
        }

        @Override
        public Iterator<Triple> describeTriples() {
            List<Triple> list = get("describeTriples", qe -> Iter.asStream(qe.describeTriples()).collect(Collectors.toList()));
            return list.iterator();
        }

        @Override
        public Iterator<Quad> constructQuads() {
            List<Quad> list = get("constructQuads", qe -> Iter.asStream(qe.constructQuads()).collect(Collectors.toList()));
            return list.iterator();
        }

        @Override
        public Iterator<JsonObject> execJsonItems() {
            List<JsonObject> list = get("jsonItems", qe -> Iter.asStream(qe.execJsonItems()).collect(Collectors.toList()));
            return list.iterator();
        }

        @Override
        public boolean ask() {
            Boolean result = get("ask", QueryExec::ask);
            return result;
        }

        // XXX How to obtain the values? Fields, ensureQExec(), cache the original QExec?

        @Override
        public DatasetGraph getDataset() {
            return null;
        }

        @Override
        public Context getContext() {
            return null;
        }

        @Override
        public Query getQuery() {
            return null;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public boolean isClosed() {
            return isClosed;
        }
    }
}
