package org.aksw.jena_sparql_api.pagination.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.extra.PaginationQueryIterator;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 11:41 PM
 */
public class QueryExecutionFactoryPaginated
    extends QueryExecutionFactoryBackQuery
{
    public static final long DEFAULT_PAGE_SIZE = 1000;

    private QueryExecutionFactory decoratee;
    private long pageSize;

    public QueryExecutionFactoryPaginated(QueryExecutionFactory decoratee) {
        this(decoratee, PaginationUtils.adjustPageSize(decoratee, DEFAULT_PAGE_SIZE));
    }

    public QueryExecutionFactoryPaginated(QueryExecutionFactory decoratee, long pageSize) {
        // Executes an ?s ?p ?o query with limit set to pageSize to
        // reduce it if necessary
        //this.pageSize = PaginationUtils.adjustPageSize(decoratee, pageSize);
        this.pageSize = pageSize;
        this.decoratee = decoratee;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
    	return createQueryExecution(decoratee, query, pageSize);
    }

    public static QueryExecution createQueryExecution(QueryExecutionFactory qef, Query query, long pageSize) {
        Query q = query.cloneQuery();
        PaginationQueryIterator queryIterator = new PaginationQueryIterator(q, pageSize);

        return new QueryExecutionIterated(query, qef, queryIterator);
    }
    
    /*
    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return decoratee.createQueryExecution(queryString);
    }*/

    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

    public long getPageSize() {
        return pageSize;
    }

    public static void main(String[] args) {
        QueryExecutionFactory factory = new QueryExecutionFactoryHttp("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
        QueryExecutionFactoryPaginated fp = new QueryExecutionFactoryPaginated(factory, 10000);

        System.out.println(fp.getPageSize());

        /*
        QueryExecution qe = fp.createQueryExecution(CannedQueryUtils.spoTemplate());

        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.next());
        }

        qe.close();
        */
    }
}
