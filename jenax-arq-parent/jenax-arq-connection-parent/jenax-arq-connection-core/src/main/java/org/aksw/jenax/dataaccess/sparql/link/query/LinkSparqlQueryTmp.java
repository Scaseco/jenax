package org.aksw.jenax.dataaccess.sparql.link.query;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalDelegate;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.Transactional;
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

    static <T> T doQueryCompute(Transactional transactional, Supplier<QueryExec> qeSupp, Function<QueryExec, T> computation) {
        T result = Txn.calculateRead(transactional, () -> {
            try (QueryExec qExec = qeSupp.get()) {
                T tmp = computation.apply(qExec);
                return tmp;
            }
        });
        return result;
    }

    static void doQueryRowSet(Transactional transactional, Supplier<QueryExec> qeSupp, Consumer<RowSet> resultSetAction) {
        Txn.executeRead(transactional, ()-> {
            try (QueryExec qExec = qeSupp.get()) {
                RowSet rs = qExec.select();
                resultSetAction.accept(rs);
            }
        });
    }

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    default void queryRowSet(String query, Consumer<RowSet> resultSetAction) {
        doQueryRowSet(this, () -> query(query), resultSetAction);
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

        doQueryRowSet(this, () -> query(query), resultSetAction);
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    default void querySelect(String query, Consumer<Binding> rowAction) {
        doQueryRowSet(this, () -> query(query), rs -> rs.forEachRemaining(rowAction));
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

        doQueryRowSet(this, () -> query(query), rs -> rs.forEachRemaining(rowAction));
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    default Graph queryConstruct(String query) {
        return doQueryCompute(this, () -> query(query), QueryExec::construct);
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    default Graph queryConstruct(Query query) {
        if ( ! query.isConstructType() )
            throw new JenaConnectionException("Query is not a CONSTRUCT query");

        return doQueryCompute(this, () -> query(query), QueryExec::construct);
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    default Graph queryDescribe(String query) {
        return doQueryCompute(this, () -> query(query), QueryExec::describe);
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    default Graph queryDescribe(Query query) {
        if ( ! query.isDescribeType() )
            throw new JenaConnectionException("Query is not a DESCRIBE query");

        return doQueryCompute(this, () -> query(query), QueryExec::describe);
    }

    /** Execute a ASK query and return a boolean */
    @Override
    default boolean queryAsk(String query) {
        return doQueryCompute(this, () -> query(query), QueryExec::ask);
    }

    /** Execute a ASK query and return a boolean */
    @Override
    default boolean queryAsk(Query query) {
        if ( ! query.isAskType() )
            throw new JenaConnectionException("Query is not a ASK query");

        return doQueryCompute(this, () -> query(query), QueryExec::ask);
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
        return newQuery().query(queryString).build();
    }
}
