package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecModDelegate;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public interface QueryExecBuilderWrapper
    extends QueryExecBuilder, QueryExecModDelegate
{
    @Override
    QueryExecBuilder getDelegate();

    @Override
    default QueryExecBuilder query(Query query) {
        getDelegate().query(query);
        return this;
    }

    /** Set the query. */
    @Override
    default QueryExecBuilder query(String queryString) {
        getDelegate().query(queryString);
        return this;
    }

    /** Set the query. */
    @Override
    default QueryExecBuilder query(String queryString, Syntax syntax) {
        getDelegate().query(queryString, syntax);
        return this;
    }

    /** Set a context entry. */
    @Override
    default QueryExecBuilder set(Symbol symbol, Object value) {
        getDelegate().set(symbol, value);
        return this;
    }

    /** Set a context entry. */
    @Override
    default QueryExecBuilder set(Symbol symbol, boolean value) {
        getDelegate().set(symbol, value);
        return this;
    }

    /**
     * Set the context. If not set, defaults to the system context
     * ({@link ARQ#getContext}).
     */
    @Override
    default QueryExecBuilder context(Context context) {
        getDelegate().context(context);
        return this;
    }

    /** Provide a set of (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    default QueryExecBuilder substitution(Binding binding) {
        getDelegate().substitution(binding);
        return this;
    }

    /** Provide a (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    default QueryExecBuilder substitution(Var var, Node value) {
        getDelegate().substitution(var, value);
        return this;
    }

    /** Provide a (var name, Node) for substitution in the query when QueryExec is built. */
    @Override
    public default QueryExecBuilder substitution(String var, Node value) {
        getDelegate().substitution(var, value);
        return this;
    }

    /** Set the overall query execution timeout. */
    @Override
    default QueryExecBuilder timeout(long value, TimeUnit timeUnit) {
        getDelegate().timeout(value, timeUnit);
        return this;
    }

    /**
     * Build the {@link QueryExec}. Further changes to he builder do not affect this
     * {@link QueryExec}.
     */
    @Override
    default QueryExec build() {
        return getDelegate().build();
    }

    // build-and-use short cuts

    /** Build and execute as a SELECT query. */
    @Override
    public default RowSet select() {
        return build().select();
    }

    /** Build and execute as a CONSTRUCT query. */
    @Override
    public default Graph construct() {
        try ( QueryExec qExec = build() ) {
            return qExec.construct();
        }
    }

    /** Build and execute as a CONSTRUCT query. */
    @Override
    public default Graph describe() {
        try ( QueryExec qExec = build() ) {
            return qExec.describe();
        }
    }

    /** Build and execute as an ASK query. */
    @Override
    public default boolean ask() {
        try ( QueryExec qExec = build() ) {
            return qExec.ask();
        }
    }
}
