package org.aksw.jena_sparql_api.cache.core;

import java.net.http.HttpConnectTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperBase;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import com.google.common.cache.Cache;

public class QueryExecutionExceptionCache
    extends QueryExecutionWrapperBase<QueryExecution>
{
    protected Cache<String, Exception> exceptionCache;

    public QueryExecutionExceptionCache(QueryExecution decoratee, Cache<String, Exception> exceptionCache) {
        super(decoratee);
        this.exceptionCache = exceptionCache;
    }

    protected String getQueryStr() {
        Query query = getQuery();
        Objects.requireNonNull(query);
        String result = query.toString();

        return result;
    }

    @Override
    protected void beforeExec() {
        String queryStr = getQueryStr();
        Exception e = exceptionCache.getIfPresent(queryStr);
        if(e != null) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onException(Exception e) {
        List<Throwable> throwables = ExceptionUtils.getThrowableList(e);
        for(Throwable t : throwables) {
            if(t instanceof TimeoutException || t instanceof HttpConnectTimeoutException) {
                String queryStr = getQueryStr();
                exceptionCache.put(queryStr, e);
            }
        }

        super.onException(e);
    }
}
