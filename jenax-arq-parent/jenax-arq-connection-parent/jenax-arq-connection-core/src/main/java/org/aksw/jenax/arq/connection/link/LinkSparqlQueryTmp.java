package org.aksw.jenax.arq.connection.link;

import java.util.function.Consumer;

import org.aksw.jenax.arq.connection.TransactionalDelegate;
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
 * Mix-in that provides default implementation of all methods based on {@link #query(Query)}
 * which in turn is based on {@link #newQuery()}.
 *
 * Possibly temporary interface; DO NOT reference it directly.
 * It will be removed should Jena move these defaults to their own interface.
 */
public interface LinkSparqlQueryTmp
    extends TransactionalDelegate, LinkSparqlQuery
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
    default void queryRowSet(String query, Consumer<RowSet> resultSetAction) {
        queryRowSet(parse(query), resultSetAction);
    }

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    default void queryRowSet(Query query, Consumer<RowSet> resultSetAction) {
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
    default void querySelect(String query, Consumer<Binding> rowAction) {
        querySelect(parse(query), rowAction);
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    default void querySelect(Query query, Consumer<Binding> rowAction) {
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
    default Graph queryConstruct(String query) {
        return queryConstruct(parse(query));
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    default Graph queryConstruct(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.construct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    default Graph queryDescribe(String query) {
        return queryDescribe(parse(query));
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    default Graph queryDescribe(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.describe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    default boolean queryAsk(String query) {
        return queryAsk(parse(query));
    }

    /** Execute a ASK query and return a boolean */
    @Override
    default boolean queryAsk(Query query) {
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
    default QueryExec query(Query query) {
        return newQuery().query(query).build();
    }

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
    default QueryExec query(String queryString) {
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
