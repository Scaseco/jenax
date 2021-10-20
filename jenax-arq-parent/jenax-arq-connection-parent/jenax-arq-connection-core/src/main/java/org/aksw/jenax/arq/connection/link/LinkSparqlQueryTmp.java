package org.aksw.jenax.arq.connection.link;

import java.util.function.Consumer;

import org.aksw.jenax.arq.connection.TransactionalTmp;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.system.Txn;

/**
 * Temporary interface; DO NOT reference it directly.
 * It will be removed once Jena moves these defaults to their own interface
 *
 * @author raven
 *
 */
public interface LinkSparqlQueryTmp
    extends TransactionalTmp, LinkSparqlQuery
{

    // ---- SparqlQueryConnection

    default Query parse(String query) {
        return QueryFactory.create(query);
    }

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    public default void queryRowSet(String query, Consumer<RowSet> resultSetAction) {
        queryRowSet(parse(query), resultSetAction);
    }

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    public default void queryRowSet(Query query, Consumer<RowSet> resultSetAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");

        Txn.executeRead(this, ()-> {
            try (QueryExec qExec = query(query) ) {
                RowSet rs = qExec.select();
                resultSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(String query, Consumer<Binding> rowAction) {
        querySelect(parse(query), rowAction);
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(Query query, Consumer<Binding> rowAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(query) ) {
                qExec.select().forEachRemaining(rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Graph queryConstruct(String query) {
        return queryConstruct(parse(query));
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Graph queryConstruct(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.construct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Graph queryDescribe(String query) {
        return queryDescribe(parse(query));
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Graph queryDescribe(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.describe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(String query) {
        return queryAsk(parse(query));
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.ask();
                }
            } );
    }

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(Query, Consumer)}, {@link #queryConstruct(Query)},
     *  {@link #queryDescribe(Query)}, {@link #queryAsk(Query)}
     *  for ways to execute queries for of a specific form.
     *
     * @param query
     * @return QueryExecution
     */
    @Override
    public QueryExec query(Query query);

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     *  {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     *  for ways to execute queries for of a specific form.
     *
     * @param queryString
     * @return QueryExecution
     */
    @Override
    public default QueryExec query(String queryString) {
        return query(parse(queryString));
    }

//
//	@Override
//	public default void querySelect(String query, Consumer<QuerySolution> rowAction) {
//		this.queryRowSet(query, rs -> {
//			while(rs.hasNext()) {
//				QuerySolution qs = rs.next();
//				rowAction.accept(qs);
//			}
//		});
//	}

}
